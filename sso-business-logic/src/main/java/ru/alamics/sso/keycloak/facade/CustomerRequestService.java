package ru.alamics.sso.keycloak.facade;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.ws.rs.core.Context;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.customer.CustomerDto;
import ru.alamics.sso.customer.CustomerService;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.tbapi.TbapiService;
import ru.alamics.sso.registration.tbapi.model.TbapiConnect;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.registration.tbapi.port.TbapiRemoteService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ApplicationScoped
@Slf4j
public class CustomerRequestService {
    private static final String CACHE_LIFESPAN_PROPERTY = "tbapi.customer.cache.lifespan.ms";
    private static final String TBAPI_REQUEST_MAX_SIZE_PROPERTY = "tbapi.customer.request.max.size";
    private static final String TBAPI_REQUEST_DISABLED_PROPERTY = "tbapi.customer.dont.request";
    private static final String TBAPI_REQUEST_LOCK_TIMEOUT_PROPERTY = "tbapi.customer.request.lock.timeout.ms";

    private final ApplicationProperties properties;
    private final TbapiService tbapiService;
    private final CustomerService customerService;

    private final Lock lock = new ReentrantLock();
    private final AtomicReference<Semaphore> semaphoreRef = new AtomicReference<>();
    private final AtomicInteger semaphoreSize = new AtomicInteger();

    @Context
    KeycloakSession session;

    public CustomerRequestService() {
        TbapiRemoteService tbapiRemoteService = Lookup.lookup(TbapiRemoteService.class);
        properties = Lookup.lookup(ApplicationProperties.class);
        tbapiService = new TbapiService(tbapiRemoteService);
        customerService = Lookup.lookup(CustomerService.class);
    }

    public String getCustomerName(String tomsId) {
        String name = getCache().get(tomsId);

        if (name != null) return name;

        CustomerDto customer = customerService.findById(tomsId);

        if (customer == null) return "";

        Instant now = Instant.now();
        Duration expire = Duration.ofMillis(properties.getPropertyInt(CACHE_LIFESPAN_PROPERTY, 15 * 60 * 1000));

        if(customer.getUpdateTime() == null
                || now.isAfter(customer.getUpdateTime().plus(expire))
                || customer.getName() == null
                || customer.getName().isEmpty()) {
            try {
                name = requestCustomerName(tomsId);

                customer = CustomerDto.builder()
                        .tomsId(tomsId)
                        .name(name)
                        .updateTime(now)
                        .build();

                if (!name.isEmpty()) {
                    customerService.save(customer);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return "";
            }
        }

        // SD-3530207: в случае неудачной попытки получения названия, не кэшировать пустое значение
        if (!customer.getName().isEmpty()) {
            getCache().put(
                    tomsId,
                    customer.getName(),
                    customer.getUpdateTime().plus(expire).toEpochMilli() - now.toEpochMilli(),
                    TimeUnit.MILLISECONDS
            );
        }

        return name;
    }

    @ActivateRequestContext
    public void updateCustomerName(String tomsId, String customerName) {
        if(customerName == null || customerName.isEmpty()) {
            getCache().remove(tomsId);
        } else {
            getCache().put(tomsId, customerName);
        }
    }

    private String requestCustomerName(String tomsId) throws InterruptedException, TimeoutException {
        boolean dontRequest = Boolean.parseBoolean(properties.getProperty(TBAPI_REQUEST_DISABLED_PROPERTY));
        if(dontRequest) {
            log.debug("FAKE customer names request due to properties: tomsId={}", tomsId);
            return "";
        }
        Semaphore semaphore = tryLock(tomsId);
        try {
            Object name = tbapiService.customerNames(new TbapiConnectConfig(TbapiConnect.CUTOMER_NAMES), List.of(tomsId)).get(tomsId);
            if (name != null) {
              return name.toString();
            }
        } catch (Exception e) {
            log.error("Fail updating customer name by tomsId={}", tomsId, e);
        } finally {
            semaphore.release();
        }
        return "";
    }

    // Rate limiter для контроля нагрузки на TBAPI
    private Semaphore tryLock(String tomsId) throws InterruptedException, TimeoutException {
        int tbapiRequestMaxSize = properties.getPropertyInt(TBAPI_REQUEST_MAX_SIZE_PROPERTY, 10);
        int requestLockTimeoutMs = properties.getPropertyInt(TBAPI_REQUEST_LOCK_TIMEOUT_PROPERTY, 30 * 1000);
        Semaphore semaphore;
        lock.lock();
        try {
            semaphore = semaphoreRef.get();
            if(semaphore == null || semaphoreSize.get() != tbapiRequestMaxSize) {
                semaphoreRef.set(semaphore = new Semaphore(tbapiRequestMaxSize));
                semaphoreSize.set(tbapiRequestMaxSize);
            }
        } finally {
            lock.unlock();
        }
        if(!semaphore.tryAcquire(requestLockTimeoutMs, TimeUnit.MILLISECONDS)) {
            throw new TimeoutException("Customer name request lock timeout for tomsId=" + tomsId);
        }
        return semaphore;
    }

    private Cache<String, String> getCache() {
        return session.getProvider(InfinispanConnectionProvider.class).getCache("customer_cache");
    }
}
