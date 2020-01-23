package ru.alamics.sso.keycloak.facade;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import ru.alamics.sso.keycloak.cache.CustomCache;
import ru.alamics.sso.keycloak.cache.impl.UserPostCache;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.service.UserPostService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class UserPostFacade {
    private static final int MAX_SIZE_POOL = 3;
    private static final String CUSTOMER_CACHE_LIFESPAN_IN_DB_PROPERTY = "user.post.cache.db.lifespan.days";
    private static final int CUSTOMER_CACHE_LIFESPAN_IN_DB = 1;

    private static final String TBAPI_REQUEST_INTERVAL_PROPERTY = "tbapi.customer.request.interval.milliseconds";
    private long TBAPI_REQUEST_INTERVAL = 10000;

    private int customerCacheLifespanInDb;
    private long tbapiRequestInterval = TBAPI_REQUEST_INTERVAL;

    private CustomCache<UserPostResponse> cache;
    @Resource(lookup = "infinispan/custom_container/customer_cache")
    private Cache<String, String> customerCache;

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
    private ConcurrentLinkedQueue<ScheduledFuture> updateCustomerTasks = new ConcurrentLinkedQueue<>();

    private UserPostService userPostService;
    private CustomerRequestService customerRequestService;
    private ApplicationProperties properties;

    @PostConstruct
    private void init() {
        try {
            tbapiRequestInterval = Long.parseLong(properties.getProperty(TBAPI_REQUEST_INTERVAL_PROPERTY));
        } finally {
            updateCustomerTasks.offer(executorService.scheduleAtFixedRate(createUpdateCustomerTask(), tbapiRequestInterval, tbapiRequestInterval, TimeUnit.MILLISECONDS));
        }
    }

    public UserPostFacade() {
        try {
            properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);

            cache = (CustomCache<UserPostResponse>) Lookup.lookup(UserPostCache.class);
            userPostService = (UserPostService) Lookup.lookup(UserPostService.class);
            customerRequestService = (CustomerRequestService) Lookup.lookup(CustomerRequestService.class);

            customerCacheLifespanInDb = Integer.parseInt(properties.getProperty(CUSTOMER_CACHE_LIFESPAN_IN_DB_PROPERTY));
        } catch (Exception e) {
            log.warn("Error parse properties file. All properties values set to default");
            customerCacheLifespanInDb = CUSTOMER_CACHE_LIFESPAN_IN_DB;
        }
    }

    public List<UserPostResponse> findByUserId(String userId) throws NotFoundException {
        Set<UserPostResponse> userPostCached = cache.get(userId);
        if (userPostCached == null || userPostCached.isEmpty()) {
            List<UserPostResponse> userPosts = userPostService.getUserPost(userId);
            addCustomersToRequest(userPosts);
            cache.put(userId, userPosts);
            return userPosts;
        }
        return userPostCached.stream().collect(Collectors.toList());
    }

    public void addUserPostAndSystemRole(UserPostRequest userPostRequest) {
        cache.put(userPostRequest.getUserId(), List.of(userPostService.addUserPostAndSystemRole(userPostRequest)));
    }

    public void clearCacheByUserId(String userId) {
        cache.clearById(userId);
    }

    public void clearCache() {
        cache.clear();
    }

    private void addCustomersToRequest(List<UserPostResponse> userPosts) {
        List<String> updatingTomsId = userPosts.stream()
                .filter(post -> !customerCache.containsKey(post.getTomsId()))
                .filter(post -> post.getUpdateTime().isBefore(LocalDateTime.now().minusDays(customerCacheLifespanInDb)) ||
                        post.getOrganization() == null)
                .map(post -> {
                    customerCache.put(post.getTomsId(), post.getOrganization());
                    return post.getTomsId();
                })
                .distinct()
                .collect(Collectors.toList());
        if (updatingTomsId.isEmpty()) {
            return;
        }
        customerRequestService.addTomsIdsInQueue(updatingTomsId);
    }

    private Runnable createUpdateCustomerTask() {
        return () -> {
            updateCustomers();
            checkLoad();
        };
    }

    private void updateCustomers() {
        Map<String, String> customers = customerRequestService.updateCustomerNames();

        //Замена во всем кэше имен организаций (ключ кэша - userId)
        cache.getAll().stream()
                .filter(post -> customers.containsKey(post.getTomsId()))
                .forEach(post -> post.setOrganization(customers.get(post.getTomsId())));
        log.info("update customers");
    }

    private void checkLoad() {
        //Добавление дополнительного потока
        if (customerRequestService.getLoadCoeff() > updateCustomerTasks.size() && updateCustomerTasks.size() < MAX_SIZE_POOL) {
            updateCustomerTasks.offer(executorService.scheduleAtFixedRate(createUpdateCustomerTask(), tbapiRequestInterval, tbapiRequestInterval, TimeUnit.MILLISECONDS));
            log.info("Increased count tasks for update customers. Count tasks={}", updateCustomerTasks.size());
            return;
        }

        //Удаление лишнего потока
        if (customerRequestService.getLoadCoeff() < updateCustomerTasks.size() - 1) {
            Objects.requireNonNull(updateCustomerTasks.poll()).cancel(true);
            log.info("Decreased count tasks for update customers. Count tasks={}", updateCustomerTasks.size());
        }
    }
}
