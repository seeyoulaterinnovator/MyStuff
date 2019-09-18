package ru.alamics.sso.keycloak.models.cache.infinispan;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.keycloak.Config;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.cache.infinispan.entities.Revisioned;
import ru.alamics.sso.keycloak.models.cache.TbapiCache;
import ru.alamics.sso.keycloak.models.cache.manager.TbapiCacheManager;

@Slf4j
public class InfinispanTbapiCacheProviderFactory implements TbapiCacheProviderFactory {
    public static final String TBAPI_CACHE_NAME = "tbapi";
    public static final String TBAPI_REVISIONS_CACHE_NAME = "tbapiRevisions";

    public static final String TBAPI_CLEAR_CACHE_EVENTS = "TBAPI_CLEAR_CACHE_EVENTS";
    public static final String TBAPI_INVALIDATION_EVENTS = "TBAPI_INVALIDATION_EVENTS";

    private KeycloakSession session;
    protected volatile TbapiCacheManager tbapiCache;

    @Override
    public TbapiCache create (KeycloakSession session) {
        this.session = session;

        return null;
    }


    private void lazyInit() {
        if(tbapiCache == null) {
            synchronized (this) {
                Cache<String, Revisioned> cache = session.getProvider(InfinispanConnectionProvider.class).getCache(InfinispanConnectionProvider.REALM_CACHE_NAME);
                Cache<String, Long> revisions = session.getProvider(InfinispanConnectionProvider.class).getCache(InfinispanConnectionProvider.REALM_REVISIONS_CACHE_NAME);
                tbapiCache = new TbapiCacheManager(cache, revisions);


                //TODO Register Events
//                ClusterProvider cluster = session.getProvider(ClusterProvider.class);
//                cluster.registerListener(REALM_INVALIDATION_EVENTS, (ClusterEvent event) -> {
//
//                    InvalidationEvent invalidationEvent = (InvalidationEvent) event;
//                    realmCache.invalidationEventReceived(invalidationEvent);
//
//                });
//
//                cluster.registerListener(REALM_CLEAR_CACHE_EVENTS, (ClusterEvent event) -> {
//
//                    realmCache.clear();
//
//                });
            }
        }
    }


    @Override
    public void init (Config.Scope config) {

    }

    @Override
    public void postInit (KeycloakSessionFactory factory) {

    }

    @Override
    public void close () {

    }

    @Override
    public String getId () {
        return "default";
    }
}
