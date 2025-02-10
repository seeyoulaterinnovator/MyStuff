package ru.alamics.sso.keycloak.cache.impl;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import lombok.Locked;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.dto.UserPostResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RequestScoped
@Slf4j
public class UserPostCache {
    @Context
    KeycloakSession session;

    @Inject
    ApplicationProperties applicationProperties;

    @Locked.Write
    public void put(String userId, List<UserPostResponse> userPosts) {
        Set<UserPostResponse> posts = getCache().get(userId);
        if (posts == null) {
            posts = new HashSet<>();
        }
        posts.addAll(userPosts);
        getCache().put(
                userId,
                posts,
                applicationProperties.getPropertyLong("cache.user_post.lifespan.ms", 5 * 60 * 1000),
                TimeUnit.MILLISECONDS
        );
    }

    @Locked.Read
    public Set<UserPostResponse> get(String userId) {
        return getCache().get(userId);
    }

    @Locked.Write
    public void clear() {
        getCache().clear();
    }

    @Locked.Write
    public void clearById(String userId) {
        Set<UserPostResponse> posts = getCache().get(userId);
        if (posts == null) {
            return;
        }
        posts.clear();
    }

    public Cache<String, Set<UserPostResponse>> getCache() {
        return session.getProvider(InfinispanConnectionProvider.class).getCache("user_post_cache");
    }
}
