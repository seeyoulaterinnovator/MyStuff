package ru.alamics.sso.keycloak.facade;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.util.validator.NotValidException;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Stateless
public class UserPostFacade {
    private static final String CUSTOMER_CACHE_LIFESPAN_IN_DB_PROPERTY = "user.post.cache.db.lifespan.days";
    private static final int CUSTOMER_CACHE_LIFESPAN_IN_DB = 1;

    private int customerCacheLifespanInDb;

    private CustomerRequestService customerRequestService;

    @Resource(lookup = "infinispan/custom_container/customer_cache")
    protected Cache<String, String> customerCache;

    protected UserPostService userPostService;

    protected ApplicationProperties properties;

    public UserPostFacade() {

        properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);

        userPostService = (UserPostService) Lookup.lookup(UserPostService.class);
        customerRequestService = (CustomerRequestService) Lookup.lookup(CustomerRequestService.class);

        customerCacheLifespanInDb = properties.getPropertyInt(CUSTOMER_CACHE_LIFESPAN_IN_DB_PROPERTY, CUSTOMER_CACHE_LIFESPAN_IN_DB, "UserPostFacade: default value used: '%s' = '%s'");
    }

    public UserPostService getUserPostService() {
        return userPostService;
    }

    public List<UserPostResponse> findByUserId(String userId) throws NotFoundException {
        List<UserPostResponse> userPosts = userPostService.getUserPost(userId);
        addCustomersToRequest(userPosts);
        return userPosts;
    }

    public UserPostResponse save(UserPostRequest userPostRequest) throws NotFoundException, FoundUserPostException, NotValidException {
        UserPostResponse post = userPostService.save(userPostRequest);
        addCustomersToRequest(Arrays.asList(post));
        return post;
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
}
