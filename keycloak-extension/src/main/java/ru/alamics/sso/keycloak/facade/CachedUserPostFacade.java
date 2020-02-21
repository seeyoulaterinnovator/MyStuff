package ru.alamics.sso.keycloak.facade;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.keycloak.cache.CustomCache;
import ru.alamics.sso.keycloak.cache.impl.UserPostCache;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;

import javax.ejb.Stateless;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
@Stateless
public class CachedUserPostFacade extends UserPostFacade {

    private CustomCache<UserPostResponse> cache;

    public CachedUserPostFacade() {
        cache = (CustomCache<UserPostResponse>) Lookup.lookup(UserPostCache.class);
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

    public void addUserPostAndSystemRole(UserPostRequest userPostRequest) {
        cache.put(userPostRequest.getUserId(), Arrays.asList(userPostService.addUserPostAndSystemRole(userPostRequest)));
    }

    public void remove(String userPostId) throws NotFoundException {
        UserPostResponse removePost = userPostService.get(userPostId);
        Set<UserPostResponse> posts = cache.get(removePost.getUserId());
        if (posts != null) {
            posts.removeIf(post -> post.getId().equals(userPostId));
        }
        userPostService.remove(userPostId);
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
}
