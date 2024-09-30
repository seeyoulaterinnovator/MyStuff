package ru.alamics.sso.keycloak.facade;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequestScoped
@Named("UserPostFacade")
@Slf4j
public class UserPostFacade {
    private static final String CUSTOMER_CACHE_LIFESPAN_IN_DB_PROPERTY = "user.post.cache.db.lifespan.days";
    private static final int CUSTOMER_CACHE_LIFESPAN_IN_DB = 1;
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

    public List<UserPostResponse> findByUserId(String userId) throws NotFoundException {
        List<UserPostResponse> userPosts = userPostService.getUserPost(userId);
        addCustomersToRequest(userPosts);
        return userPosts;
    }

    public UserPostResponse save(UserPostRequest userPostRequest) throws NotFoundException, FoundUserPostException, NotValidException {
        UserPostResponse post = userPostService.save(userPostRequest);
        addCustomersToRequest(Collections.singletonList(post));
        return post;
    }

    protected Cache<String, String> getCustomerCache() {
        return session.getProvider(InfinispanConnectionProvider.class).getCache("customer_cache");
    }

    private void addCustomersToRequest(List<UserPostResponse> userPosts) {
        List<String> updatingTomsId = userPosts.stream()
                .filter(post -> !getCustomerCache().containsKey(post.getTomsId()))
                .filter(post -> !post.getUpdateTime().isBefore(LocalDateTime.now().minusHours(customerCacheLifespanInDb)) ||
                        post.getOrganization() == null)
                .map(post -> {
                    getCustomerCache().put(post.getTomsId(), post.getOrganization() == null ? " " : post.getOrganization());
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
