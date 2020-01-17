package ru.alamics.sso.keycloak.facade;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.customer.CustomerDto;
import ru.alamics.sso.keycloak.cache.CustomCache;
import ru.alamics.sso.keycloak.cache.UserPostCache;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.service.UserPostService;

import javax.ejb.Singleton;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class UserPostFacade {

    private static final String CUSTOMER_CACHE_LIFESPAN_IN_DB_PROPERTY = "user.post.cache.db.lifespan.days";
    private int CUSTOMER_CACHE_LIFESPAN_IN_DB = 1;
    private int customerCacheLifespanInDb;

    private CustomCache<UserPostResponse> cache;

    private UserPostService userPostService;
    private CustomerRequestService customerRequestService;
    private ApplicationProperties properties;

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
                .filter(post -> post.getUpdateTime().isBefore(LocalDateTime.now().minusDays(customerCacheLifespanInDb)) || post.getOrganization() == null)
                .map(UserPostResponse::getTomsId)
                .distinct()
                .collect(Collectors.toList());
        if (updatingTomsId.isEmpty()) {
            return;
        }
        customerRequestService.addTomsIdsInQueue(updatingTomsId);
    }

    public void updateCustomers() {
        Map<String, String> customers = customerRequestService.updateCustomerNames().stream()
                .collect(Collectors.toMap(CustomerDto::getTomsId, CustomerDto::getName));
        cache.getAll().stream()
                .filter(post -> customers.containsKey(post.getTomsId()))
                .forEach(post -> {
                    String newOrgName = post.getOrganization();
                    if (customers.get(post.getTomsId()) != null) {
                        newOrgName = customers.get(post.getTomsId());
                    } else if (newOrgName == null) {
                        newOrgName = " ";
                    }
                    post.setOrganization(newOrgName);
                });
        log.debug("Finished update customers");
    }
}
