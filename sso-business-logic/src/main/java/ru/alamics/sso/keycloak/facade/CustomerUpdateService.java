package ru.alamics.sso.keycloak.facade;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
@Startup
@Singleton
public class CustomerUpdateService {
    private static final int MAX_SIZE_POOL = 1;
    private static final String TBAPI_REQUEST_INTERVAL_PROPERTY = "tbapi.customer.request.interval.milliseconds";
    private long TBAPI_REQUEST_INTERVAL_DEFAULT = 10000;

    private CustomerRequestService customerRequestService;
    @Resource(lookup = "infinispan/custom_container/customer_cache")
    private Cache<String, String> customerCache;

    private long tbapiRequestInterval;

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(MAX_SIZE_POOL);
    private ConcurrentLinkedQueue<ScheduledFuture> tasksPool = new ConcurrentLinkedQueue<>();

    public CustomerUpdateService() {
        customerRequestService = (CustomerRequestService) Lookup.lookup(CustomerRequestService.class);
    }

    @PostConstruct
    private void init() {

        ApplicationProperties properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);
        tbapiRequestInterval = properties.getPropertyLong(TBAPI_REQUEST_INTERVAL_PROPERTY, TBAPI_REQUEST_INTERVAL_DEFAULT, "CustomerUpdateService: default value used: '%s' = '%s'");
        log.info("tbapiRequestInterval set to value={}", tbapiRequestInterval);

        tasksPool.offer(executorService.scheduleAtFixedRate(new UpdateTask(), tbapiRequestInterval, tbapiRequestInterval, TimeUnit.MILLISECONDS));
    }

    @Lock(LockType.READ)
    public int getTasksPoolSize() {
        return tasksPool.size();
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
