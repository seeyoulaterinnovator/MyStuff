package ru.alamics.sso.keycloak.models.cache.manager;

import org.infinispan.Cache;
import org.jboss.logging.Logger;
import org.keycloak.models.cache.infinispan.CacheManager;
import org.keycloak.models.cache.infinispan.entities.Revisioned;
import org.keycloak.models.cache.infinispan.events.InvalidationEvent;
import ru.alamics.sso.keycloak.models.cache.TbapiCacheInvalidationEvent;
import ru.alamics.sso.keycloak.models.cache.impl.TbapiCacheSession;

import java.util.Set;

public class TbapiCacheManager extends CacheManager {
    private static final Logger logger = Logger.getLogger(TbapiCacheManager.class);

    public TbapiCacheManager (Cache<String, Revisioned> cache,
                              Cache<String, Long> revisions) {
        super(cache, revisions);
    }

    @Override
    protected Logger getLogger () {
        return logger;
    }

    @Override
    protected void addInvalidationsFromEvent (InvalidationEvent event, Set<String> invalidations) {
        invalidations.add(event.getId());

//        ((TbapiCacheInvalidationEvent) event).addInvalidations(this, invalidations);
    }

    public void clientAdded(String clientId, Set<String> invalidations) {
        invalidations.add(TbapiCacheSession.getCustomerCacheKey(clientId));
    }
}
