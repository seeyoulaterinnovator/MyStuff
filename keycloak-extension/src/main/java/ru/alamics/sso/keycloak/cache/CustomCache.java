package ru.alamics.sso.keycloak.cache;

import ru.alamics.sso.registration.dto.UserPostResponse;

import java.util.List;
import java.util.Set;

public interface CustomCache<T> {

    void put(String key, List<T> values);

    Set<UserPostResponse> get(String key);

    void clear();

    void clearById(String key);

}
