package ru.alamics.sso.keycloak.cache;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import ru.alamics.sso.registration.dto.UserPostResponse;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
@Startup
@Slf4j
public class UserPostCache implements CustomCache<UserPostResponse> {

    @Resource(lookup = "infinispan/custom_container/custom_cache")
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
    public Set<UserPostResponse> getAll(String userId) {
        return cache.get(userId);
    }
}
