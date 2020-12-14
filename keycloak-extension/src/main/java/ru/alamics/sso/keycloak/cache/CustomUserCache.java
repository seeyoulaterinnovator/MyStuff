package ru.alamics.sso.keycloak.cache;

import org.infinispan.Cache;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.cluster.ClusterEvent;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.cache.UserCache;
import org.keycloak.models.cache.UserCacheProviderFactory;
import org.keycloak.models.cache.infinispan.UserCacheManager;
import org.keycloak.models.cache.infinispan.entities.Revisioned;
import org.keycloak.models.cache.infinispan.events.InvalidationEvent;

public class CustomUserCache implements UserCacheProviderFactory {

    public static final String USER_CLEAR_CACHE_EVENTS = "USER_CLEAR_CACHE_EVENTS";
    public static final String USER_INVALIDATION_EVENTS = "USER_INVALIDATION_EVENTS";
    private static final Logger log = Logger.getLogger(org.keycloak.models.cache.infinispan.InfinispanUserCacheProviderFactory.class);
    protected volatile UserCacheManager userCache;


    @Override
    public UserCache create(KeycloakSession session) {
        lazyInit(session);
        return new CustomUserCacheSession(userCache, session);
    }

    private void lazyInit(KeycloakSession session) {
        if (userCache == null) {
            synchronized (this) {
                if (userCache == null) {
                    Cache<String, Revisioned> cache = session.getProvider(InfinispanConnectionProvider.class).getCache(InfinispanConnectionProvider.USER_CACHE_NAME);
                    Cache<String, Long> revisions = session.getProvider(InfinispanConnectionProvider.class).getCache(InfinispanConnectionProvider.USER_REVISIONS_CACHE_NAME);
                    userCache = new UserCacheManager(cache, revisions);

                    ClusterProvider cluster = session.getProvider(ClusterProvider.class);

                    cluster.registerListener(USER_INVALIDATION_EVENTS, (ClusterEvent event) -> {

                        InvalidationEvent invalidationEvent = (InvalidationEvent) event;
                        userCache.invalidationEventReceived(invalidationEvent);

                    });

                    cluster.registerListener(USER_CLEAR_CACHE_EVENTS, (ClusterEvent event) -> {

                        userCache.clear();

                    });

                    log.debug("Registered cluster listeners");
                }
            }
        }
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "default";
    }

   /* @Override
    public int order() {
        return 1;
    }*/
}