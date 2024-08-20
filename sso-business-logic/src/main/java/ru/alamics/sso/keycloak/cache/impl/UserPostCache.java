package ru.alamics.sso.keycloak.cache.impl;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import lombok.Locked;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.registration.dto.UserPostResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
@Slf4j
public class UserPostCache {
    @Context
    KeycloakSession session;

    private Cache<String, Set<UserPostResponse>> cache;

    @PostConstruct
    void init() {
        cache = session.getProvider(InfinispanConnectionProvider.class).getCache("user_post_cache");
    }

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
