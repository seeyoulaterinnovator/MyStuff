package ru.alamics.sso.keycloak.cache;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.UserCache;
import org.keycloak.models.cache.infinispan.UserCacheManager;
import org.keycloak.models.cache.infinispan.UserCacheSession;
import org.keycloak.models.cache.infinispan.entities.CachedUser;

public class CustomUserCacheSession extends UserCacheSession implements UserCache {

    public CustomUserCacheSession(UserCacheManager cache, KeycloakSession session) {
        super(cache, session);
        this.cache = cache;
        this.session = session;
    }

    @Override
    public UserModel getUserById(String id, RealmModel realm) {
        if (isRegisteredForInvalidation(realm, id)) {
            return getDelegate().getUserById(id, realm);
        }
        if (managedUsers.containsKey(id)) {
            return managedUsers.get(id);
        }

        CachedUser cached = cache.get(id, CachedUser.class);
        UserModel adapter = null;
        if (cached == null) {
            Long loaded = cache.getCurrentRevision(id);
            UserModel delegate = getDelegate().getUserById(id, realm);
            if (delegate == null) {
                return null;
            }
            adapter = cacheUser(realm, delegate, loaded);
        } else {
            adapter = validateCache(realm, cached);
            if (adapter == null) {
                session.userCache().clear();
                Long loaded = cache.getCurrentRevision(id);
                UserModel delegate = getDelegate().getUserById(id, realm);
                if (delegate == null) {
                    return null;
                }
                adapter = cacheUser(realm, delegate, loaded);
            }
        }
        managedUsers.put(id, adapter);
        return adapter;
    }

    private boolean isRegisteredForInvalidation(RealmModel realm, String userId) {
        return realmInvalidations.contains(realm.getId()) || invalidations.contains(userId);
    }
}
