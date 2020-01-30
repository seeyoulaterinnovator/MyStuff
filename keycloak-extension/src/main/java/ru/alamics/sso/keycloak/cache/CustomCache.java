package ru.alamics.sso.keycloak.cache;

import java.util.List;
import java.util.Set;

public interface CustomCache<T> {

    void put(String key, List<T> values);

    Set<T> get(String key);

    Set<T> getAll();

    void clear();

    void clearById(String key);

}
