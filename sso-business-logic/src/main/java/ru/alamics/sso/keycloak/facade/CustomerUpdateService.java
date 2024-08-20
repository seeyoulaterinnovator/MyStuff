package ru.alamics.sso.keycloak.facade;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.core.Context;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

@ApplicationScoped
@Slf4j
public class CustomerUpdateService {
    private static final int MAX_SIZE_POOL = 1;
    private static final String TBAPI_REQUEST_INTERVAL_PROPERTY = "tbapi.customer.request.interval.milliseconds";
    private static final long TBAPI_REQUEST_INTERVAL_DEFAULT = 10000;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(MAX_SIZE_POOL);

    private final ConcurrentLinkedQueue<ScheduledFuture> tasksPool = new ConcurrentLinkedQueue<>();

    private CustomerRequestService customerRequestService;

    @Context
    KeycloakSession session;

    private Cache<String, String> customerCache;

    private long tbapiRequestInterval;

    public CustomerUpdateService() {
        customerRequestService = Lookup.lookup(CustomerRequestService.class);
    }

    @PostConstruct
    void init() {
        ApplicationProperties properties = Lookup.lookup(ApplicationProperties.class);
        tbapiRequestInterval = properties.getPropertyLong(TBAPI_REQUEST_INTERVAL_PROPERTY, TBAPI_REQUEST_INTERVAL_DEFAULT, "CustomerUpdateService: default value used: '%s' = '%s'");
        log.info("tbapiRequestInterval set to value={}", tbapiRequestInterval);

        customerCache = session.getProvider(InfinispanConnectionProvider.class).getCache("customer_cache");
    }

    void onStart(@Observes StartupEvent ev) {
        tasksPool.offer(executorService.scheduleAtFixedRate(new UpdateTask(), tbapiRequestInterval, tbapiRequestInterval, TimeUnit.MILLISECONDS));
    }

    private class UpdateTask implements Runnable {
        @Override
        public void run() {
            updateCustomers();
            checkLoad();
        }
    }

    private void updateCustomers() {
        Map<String, String> customers = customerRequestService.updateCustomerNames();

        if (customers.isEmpty()) {
            return;
        }

        //Замена во всем кэше имен организаций (ключ кэша - tomsId)
        customers.entrySet().stream()
                .filter(customer -> customer.getValue() != null && !customer.getValue().isEmpty())
                .forEach(customer -> customerCache.put(customer.getKey(), customer.getValue()));
        log.info("customers update is finished");
    }

    private void checkLoad() {
        //Добавление дополнительного потока
        if (customerRequestService.getLoadCoeff() > tasksPool.size() && tasksPool.size() < MAX_SIZE_POOL) {
            tasksPool.offer(executorService.scheduleAtFixedRate(new UpdateTask(), tbapiRequestInterval, tbapiRequestInterval, TimeUnit.MILLISECONDS));
            log.info("Increased count tasks for update customers. Count tasks={}", tasksPool.size());
            return;
        }

        //Удаление лишнего потока
        if (customerRequestService.getLoadCoeff() < tasksPool.size() - 1) {
            Objects.requireNonNull(tasksPool.poll()).cancel(true);
            log.info("Decreased count tasks for update customers. Count tasks={}", tasksPool.size());
        }
    }
}
