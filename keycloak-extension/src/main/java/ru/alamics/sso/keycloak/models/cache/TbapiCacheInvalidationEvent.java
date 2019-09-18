package ru.alamics.sso.keycloak.models.cache;

import ru.alamics.sso.keycloak.models.cache.manager.TbapiCacheManager;

import java.util.Set;

public interface TbapiCacheInvalidationEvent {
    void addInvalidations(TbapiCacheManager userCache, Set<String> invalidations);
}
