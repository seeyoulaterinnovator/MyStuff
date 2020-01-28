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

import javax.annotation.Resource;
import javax.ejb.Singleton;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class UserPostFacade {
    private static final String CUSTOMER_CACHE_LIFESPAN_IN_DB_PROPERTY = "user.post.cache.db.lifespan.days";
    private static final int CUSTOMER_CACHE_LIFESPAN_IN_DB = 1;

    private int customerCacheLifespanInDb;

    private CustomCache<UserPostResponse> cache;
    @Resource(lookup = "infinispan/custom_container/customer_cache")
    private Cache<String, String> customerCache;

    private UserPostService userPostService;
    private CustomerRequestService customerRequestService;
    private ApplicationProperties properties;
    private CustomerUpdateService customerUpdateService;

    public UserPostFacade() {
        try {
            properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);

            cache = (CustomCache<UserPostResponse>) Lookup.lookup(UserPostCache.class);
            userPostService = (UserPostService) Lookup.lookup(UserPostService.class);
            customerRequestService = (CustomerRequestService) Lookup.lookup(CustomerRequestService.class);
            customerUpdateService = (CustomerUpdateService) Lookup.lookup(CustomerUpdateService.class);

            customerCacheLifespanInDb = Integer.parseInt(properties.getProperty(CUSTOMER_CACHE_LIFESPAN_IN_DB_PROPERTY));
        } catch (Exception e) {
            log.warn("Error parse properties file. All properties values set to default");
            customerCacheLifespanInDb = CUSTOMER_CACHE_LIFESPAN_IN_DB;
        }
    }

    public List<UserPostResponse> findByUserId(String userId) throws NotFoundException {
        List<UserPostResponse> userPostCached = getCachedUserPosts(userId);
        if (userPostCached.isEmpty()) {
            List<UserPostResponse> userPosts = userPostService.getUserPost(userId);
            addCustomersToRequest(userPosts);
            cache.put(userId, userPosts);
            return userPosts;
        }
        return userPostCached;
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
                    customerCache.put(post.getTomsId(), post.getOrganization() == null ? " " : post.getOrganization());
                    return post.getTomsId();
                })
                .distinct()
                .collect(Collectors.toList());
        if (updatingTomsId.isEmpty()) {
            return;
        }
        customerRequestService.addTomsIdsInQueue(updatingTomsId);
    }

    private List<UserPostResponse> getCachedUserPosts(String userId) {
        Set<UserPostResponse> cachedPosts = cache.get(userId);
        if (cachedPosts == null || cachedPosts.isEmpty()) {
            return new LinkedList<>();
        }
        cachedPosts.stream()
                .filter(post -> customerCache.get(post.getTomsId()) != null && !customerCache.get(post.getTomsId()).isBlank())
                .forEach(post -> post.setOrganization(customerCache.get(post.getTomsId())));
        return new LinkedList<>(cachedPosts);
    }
}
