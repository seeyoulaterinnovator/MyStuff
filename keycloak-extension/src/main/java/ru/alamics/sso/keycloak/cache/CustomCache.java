package ru.alamics.sso.keycloak.cache;

import ru.alamics.sso.registration.dto.UserPostResponse;

import java.util.List;
import java.util.Set;

public interface CustomCache<T> {

    void put(String userId, List<T> userPosts);

    Set<UserPostResponse> getAll(String key);
}
