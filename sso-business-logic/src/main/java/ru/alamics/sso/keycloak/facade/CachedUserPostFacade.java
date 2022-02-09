package ru.alamics.sso.keycloak.facade;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.keycloak.cache.CustomCache;
import ru.alamics.sso.keycloak.cache.impl.UserPostCache;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.ExternalSystemRoleRequest;
import ru.alamics.sso.registration.dto.UserPostEditRequest;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.util.validator.NotValidException;

import javax.ejb.Stateless;
import javax.ws.rs.NotFoundException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
@Stateless
public class CachedUserPostFacade extends UserPostFacade {

    private final CustomCache<UserPostResponse> cache;

    public CachedUserPostFacade() {
        cache = Lookup.lookup(UserPostCache.class);
    }

    public List<UserPostResponse> findByUserId(String userId) throws NotFoundException {
        List<UserPostResponse> userPostCached = getCachedUserPosts(userId);

        if (userPostCached.isEmpty()) {
            List<UserPostResponse> userPosts = super.findByUserId(userId);
            cache.put(userId, userPosts);
            return userPosts;
        }

        return userPostCached;
    }

    public void addUserPostAndSystemRole(UserPostRequest userPostRequest) throws NotFoundException, FoundUserPostException, NotValidException {
        cache.put(userPostRequest.getUserId(), Arrays.asList(userPostService.addUserPostAndAllSystemRole(userPostRequest)));
    }

    public UserPostResponse save(UserPostRequest userPostRequest) throws NotFoundException, FoundUserPostException, NotValidException {
        UserPostResponse post = super.save(userPostRequest);

        if (cache.get(userPostRequest.getUserId()) != null) {
            cache.put(userPostRequest.getUserId(), Arrays.asList(post));
        }

        return post;
    }

    public UserPostResponse edit(UserPostEditRequest userPostRequest) throws NotFoundException {
        UserPostResponse post = userPostService.edit(userPostRequest);

        updateCachedUserPost(post);

        return post;
    }

    public void remove(String userPostId) throws NotFoundException {
        UserPostResponse removePost = userPostService.get(userPostId);

        Set<UserPostResponse> posts = cache.get(removePost.getUserId());
        if (posts != null) {
            posts.removeIf(post -> post.getId().equals(userPostId));
        }

        userPostService.remove(userPostId);
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
        cache.clearById(userId);
    }

    public void clearCache() {
        cache.clear();
    }

    private List<UserPostResponse> getCachedUserPosts(String userId) {
        Set<UserPostResponse> cachedPosts = cache.get(userId);
        if (cachedPosts == null || cachedPosts.isEmpty()) {
            return new LinkedList<>();
        }

        cachedPosts.stream()
                .filter(post -> customerCache.get(post.getTomsId()) != null && !customerCache.get(post.getTomsId()).isEmpty())
                .forEach(post -> post.setOrganization(customerCache.get(post.getTomsId())));

        return new LinkedList<>(cachedPosts);
    }

    private void updateCachedUserPost(UserPostResponse post) {
        Set<UserPostResponse> cachedPosts = cache.get(post.getUserId());

        if (cachedPosts != null) {
            cachedPosts.removeIf(cpost -> cpost.getId().equals(post.getId()));
            cachedPosts.add(post);
        }
    }

}
