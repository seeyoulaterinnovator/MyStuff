package ru.alamics.sso.keycloak.facade;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
public class CustomerUpdateTask implements Runnable {
    private static final int MAX_SIZE_POOL = 3;
    private static final String TBAPI_REQUEST_INTERVAL_PROPERTY = "tbapi.customer.request.interval.milliseconds";
    private long TBAPI_REQUEST_INTERVAL_DEFAULT = 10000;

    private final CustomerRequestService customerRequestService;
    private final Cache<String, String> customerCache;

    private long tbapiRequestInterval;

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(MAX_SIZE_POOL);
    private ConcurrentLinkedQueue<ScheduledFuture> tasksPool = new ConcurrentLinkedQueue<>();

    public CustomerUpdateTask(CustomerRequestService customerRequestService, Cache<String, String> customerCache,
                              ScheduledExecutorService executorService, ConcurrentLinkedQueue<ScheduledFuture> tasksPool) {
        this.customerRequestService = customerRequestService;
        this.customerCache = customerCache;

        if (executorService != null) {
            this.executorService = executorService;
        }

        if (tasksPool != null) {
            this.tasksPool = tasksPool;
        }

        init();
    }

    private void init() {
        try {
            ApplicationProperties properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);
            tbapiRequestInterval = Long.parseLong(properties.getProperty(TBAPI_REQUEST_INTERVAL_PROPERTY));
            log.warn("tbapiRequestInterval set to value={}", tbapiRequestInterval);
        } catch (Exception e) {
            tbapiRequestInterval = TBAPI_REQUEST_INTERVAL_DEFAULT;
            log.warn("tbapiRequestInterval set to default value={}", tbapiRequestInterval);
        }
        tasksPool.offer(executorService.scheduleAtFixedRate(this, tbapiRequestInterval, tbapiRequestInterval, TimeUnit.MILLISECONDS));
    }

    @Override
    public void run() {
        updateCustomers();
        checkLoad();
    }

    private void updateCustomers() {
        Map<String, String> customers = customerRequestService.updateCustomerNames();

        if (customers.isEmpty()) {
            return;
        }

        //Замена во всем кэше имен организаций (ключ кэша - tomsId)
        customers.entrySet().stream()
                .filter(customer -> customer.getValue() != null && !customer.getValue().isBlank())
                .forEach(customer -> customerCache.put(customer.getKey(), customer.getValue()));
        log.info("customers update is finished");
    }

    private void checkLoad() {
        //Добавление дополнительного потока
        if (customerRequestService.getLoadCoeff() > tasksPool.size() && tasksPool.size() < MAX_SIZE_POOL) {
            new CustomerUpdateTask(customerRequestService, customerCache, executorService, tasksPool);
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
