package ru.alamics.sso.keycloak.cache.impl;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Locked;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import ru.alamics.sso.registration.dto.UserPostResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
@Slf4j
public class UserPostCache {
    @Resource(lookup = "infinispan/custom_container/user_post_cache") // TODO upgrade: check
    private Cache<String, Set<UserPostResponse>> cache;

    @Locked.Write
    public void put(String userId, List<UserPostResponse> userPosts) {
        Set<UserPostResponse> posts = cache.get(userId);
        if (posts == null) {
            posts = new HashSet<>();
        }
        posts.addAll(userPosts);
        cache.put(userId, posts);
    }

    @Locked.Read
    public Set<UserPostResponse> get(String userId) {
        return cache.get(userId);
    }

    public void clear() {
        cache.clear();
    }

    public void clearById(String userId) {
        Set<UserPostResponse> posts = cache.get(userId);
        if (posts == null) {
            return;
        }
        posts.clear();
    }
}
