package ru.alamics.sso.keycloak.cache;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import ru.alamics.sso.registration.dto.UserPostResponse;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Startup
@Slf4j
public class UserPostCache implements CustomCache<UserPostResponse> {

    @Resource(lookup = "infinispan/custom_container/user_post_cache")
    private Cache<String, Set<UserPostResponse>> cache;

    @Override
    public void put(String userId, List<UserPostResponse> userPosts) {
        Set<UserPostResponse> posts = cache.get(userId);
        if (posts == null) {
            posts = new HashSet<>();
        }
        posts.addAll(userPosts);
        cache.put(userId, posts);
    }

    @Override
    public Set<UserPostResponse> get(String userId) {
        return cache.get(userId);
    }

    @Override
    public Set<UserPostResponse> getAll() {
        return cache.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public void clearById(String userId) {
        Set<UserPostResponse> posts = cache.get(userId);
        if (posts == null) {
            return;
        }
        posts.clear();
    }
}
