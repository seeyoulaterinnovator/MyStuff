package ru.alamics.sso.keycloak.facade;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.util.validator.NotValidException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class UserPostFacade {
    private static final String CUSTOMER_CACHE_LIFESPAN_IN_DB_PROPERTY = "user.post.cache.db.lifespan.days";
    private static final int CUSTOMER_CACHE_LIFESPAN_IN_DB = 1;
    protected Cache<String, String> customerCache;
    @Getter
    protected UserPostService userPostService;
    protected ApplicationProperties properties;
    private final int customerCacheLifespanInDb;
    private final CustomerRequestService customerRequestService;

    @Context
    KeycloakSession session;

    public UserPostFacade() {

        properties = Lookup.lookup(ApplicationProperties.class);

        userPostService = Lookup.lookup(UserPostService.class);
        customerRequestService = Lookup.lookup(CustomerRequestService.class);

        customerCacheLifespanInDb = properties.getPropertyInt(CUSTOMER_CACHE_LIFESPAN_IN_DB_PROPERTY, CUSTOMER_CACHE_LIFESPAN_IN_DB, "UserPostFacade: default value used: '{}' = '{}'");
    }

    @PostConstruct
    void init() {
        customerCache = session.getProvider(InfinispanConnectionProvider.class).getCache("customer_cache");
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
                .filter(post -> post.getUpdateTime().isBefore(LocalDateTime.now().minusHours(customerCacheLifespanInDb)) ||
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
