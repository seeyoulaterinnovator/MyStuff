/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.alamics.sso.keycloak.cache;

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.cache.UserCache;
import org.keycloak.models.cache.infinispan.UserCacheManager;
import org.keycloak.models.cache.infinispan.UserCacheSession;
import org.keycloak.models.cache.infinispan.entities.CachedUser;
import org.keycloak.models.cache.infinispan.events.InvalidationEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CustomUserCacheSession extends UserCacheSession implements UserCache {
    protected static final Logger logger = Logger.getLogger(CustomUserCacheSession.class);
    protected final long startupRevision;
    protected UserCacheManager cache;
    protected KeycloakSession session;
    protected UserProvider delegate;
    protected boolean transactionActive;
    protected boolean setRollbackOnly;
    protected Set<String> invalidations = new HashSet<>();
    protected Set<String> realmInvalidations = new HashSet<>();
    protected Set<InvalidationEvent> invalidationEvents = new HashSet<>(); // Events to be sent across cluster
    protected Map<String, UserModel> managedUsers = new HashMap<>();

    public CustomUserCacheSession(UserCacheManager cache, KeycloakSession session) {
        super(cache, session);
        this.cache = cache;
        this.session = session;
        this.startupRevision = cache.getCurrentCounter();
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
