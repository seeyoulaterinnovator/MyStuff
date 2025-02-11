package ru.alamics.sso.keycloak.facade;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.ExternalSystemRoleRequest;
import ru.alamics.sso.registration.dto.UserPostEditRequest;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.util.validator.NotValidException;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequestScoped
@Named("CachedUserPostFacade")
@Slf4j
public class CachedUserPostFacade extends UserPostFacade {
    @Context
    KeycloakSession session;

    @Inject
    ApplicationProperties applicationProperties;

    private final CustomerRequestService customerRequestService;

    public CachedUserPostFacade() {
        customerRequestService = Lookup.lookup(CustomerRequestService.class);
    }

    @Override
    public List<UserPostResponse> findByUserId(String userId) throws NotFoundException {
        List<UserPostResponse> posts = findCachedByUserId(userId);
        for(var post : posts) {
            var customer = customerRequestService.getCustomerName(post.getTomsId());
            if(customer != null && !customer.isEmpty()) {
                post.setOrganization(customer);
            }
        }
        return posts.stream()
                .sorted(Comparator.comparing(UserPostResponse::getId))
                .collect(Collectors.toList());
    }

    public void addUserPostAndSystemRole(UserPostRequest userPostRequest) throws NotFoundException, FoundUserPostException, NotValidException {
        UserPostResponse post = userPostService.addUserPostAndAllSystemRole(userPostRequest);
        updateCachedUserPost(post);
    }

    public UserPostResponse save(UserPostRequest userPostRequest) throws NotFoundException, FoundUserPostException, NotValidException {
        UserPostResponse post = super.save(userPostRequest);
        updateCachedUserPost(post);
        return post;
    }

    public UserPostResponse edit(UserPostEditRequest userPostRequest) throws NotFoundException {
        UserPostResponse post = userPostService.edit(userPostRequest);
        updateCachedUserPost(post);
        return post;
    }

    public void remove(String userPostId) throws NotFoundException {
        UserPostResponse post = userPostService.get(userPostId);
        if (post != null) {
            userPostService.remove(userPostId);
            List<UserPostResponse> posts = findCachedByUserId(post.getUserId());
            posts.removeIf(p -> p.getId().equals(post.getId()));
            getCache().put(post.getUserId(), posts);
        }
    }

    public UserPostResponse addSystemRole(ExternalSystemRoleRequest externalSystemRoleRequest) throws NotFoundException {
        UserPostResponse post = userPostService.addSystemRole(externalSystemRoleRequest);
        updateCachedUserPost(post);
        return post;
    }

    public UserPostResponse removeSystemRole(ExternalSystemRoleRequest externalSystemRoleRequest) throws NotFoundException {
        UserPostResponse post = userPostService.removeSystemRole(externalSystemRoleRequest);
        updateCachedUserPost(post);
        return post;
    }

    public void clearCacheByUserId(String userId) {
        getCache().remove(userId);
    }

    public void clearCache() {
        getCache().clear();
    }

    private List<UserPostResponse> findCachedByUserId(String userId) throws NotFoundException {
        List<UserPostResponse> posts = null;
        try {
            posts = getCache().get(userId);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        if (posts == null) {
            posts = super.findByUserId(userId);
            getCache().put(
                    userId,
                    posts,
                    applicationProperties.getPropertyLong("cache.user_post.lifespan.ms", 5 * 60 * 1000),
                    TimeUnit.MILLISECONDS
            );
        }
        return posts;
    }

    private void updateCachedUserPost(UserPostResponse post) {
        List<UserPostResponse> posts = findCachedByUserId(post.getUserId());
        posts.removeIf(p -> p.getId().equals(post.getId()));
        posts.add(post);
        getCache().put(post.getUserId(), posts);
    }

    private Cache<String, List<UserPostResponse>> getCache() {
        return session.getProvider(InfinispanConnectionProvider.class).getCache("user_post_cache");
    }
}
