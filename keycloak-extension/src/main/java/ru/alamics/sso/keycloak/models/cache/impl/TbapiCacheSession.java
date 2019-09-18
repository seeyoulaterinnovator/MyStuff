package ru.alamics.sso.keycloak.models.cache.impl;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransaction;
import org.keycloak.models.cache.infinispan.events.InvalidationEvent;
import ru.alamics.sso.keycloak.models.cache.CacheTbapiProvider;
import ru.alamics.sso.keycloak.models.cache.infinispan.InfinispanTbapiCacheProviderFactory;
import ru.alamics.sso.keycloak.models.cache.manager.TbapiCacheManager;

import java.util.HashSet;
import java.util.Set;

public class TbapiCacheSession implements CacheTbapiProvider {
    public static final String CUSTOMER_CACHE_SUFFIX = ".customer";

    private TbapiCacheManager cache;
    private KeycloakSession session;
    private Set<String> invalidations = new HashSet<>();
    private Set<InvalidationEvent> invalidationEvents = new HashSet<>(); // Events to be sent across cluster

    private boolean transactionActive;
    private boolean setRollbackOnly;
    private boolean clearAll;

    public TbapiCacheSession (TbapiCacheManager cache, KeycloakSession session) {
        this.cache = cache;
        this.session = session;
        session.getTransactionManager().enlistPrepare(getPrepareTransaction());
        session.getTransactionManager().enlistAfterCompletion(getAfterTransaction());
    }

    @Override
    public void close () {
    }


    public static String getCustomerCacheKey(String client) {
        return client + CUSTOMER_CACHE_SUFFIX;
    }

    public void addCustomer(String customerId) {

    }

    private KeycloakTransaction getPrepareTransaction() {
        return new KeycloakTransaction() {
            @Override
            public void begin() {
                transactionActive = true;
            }

            @Override
            public void commit() {
                /*  THIS WAS CAUSING DEADLOCK IN A CLUSTER
                if (delegate == null) return;
                List<String> locks = new LinkedList<>();
                locks.addAll(invalidations);

                Collections.sort(locks); // lock ordering
                cache.getRevisions().startBatch();

                if (!locks.isEmpty()) cache.getRevisions().getAdvancedCache().lock(locks);
                */

            }

            @Override
            public void rollback() {
                setRollbackOnly = true;
                transactionActive = false;
            }

            @Override
            public void setRollbackOnly() {
                setRollbackOnly = true;
            }

            @Override
            public boolean getRollbackOnly() {
                return setRollbackOnly;
            }

            @Override
            public boolean isActive() {
                return transactionActive;
            }
        };
    }

    private KeycloakTransaction getAfterTransaction() {
        return new KeycloakTransaction() {
            @Override
            public void begin() {
                transactionActive = true;
            }

            @Override
            public void commit() {
                try {
                    if (clearAll) {
                        cache.clear();
                    }
                    runInvalidations();
                    transactionActive = false;
                } finally {
                    cache.endRevisionBatch();
                }
            }

            @Override
            public void rollback() {
                try {
                    setRollbackOnly = true;
                    runInvalidations();
                    transactionActive = false;
                } finally {
                    cache.endRevisionBatch();
                }
            }

            @Override
            public void setRollbackOnly() {
                setRollbackOnly = true;
            }

            @Override
            public boolean getRollbackOnly() {
                return setRollbackOnly;
            }

            @Override
            public boolean isActive() {
                return transactionActive;
            }
        };
    }

    private void runInvalidations () {
        for (String id : invalidations) {
            cache.invalidateObject(id);
        }
        cache.sendInvalidationEvents(session, invalidationEvents, InfinispanTbapiCacheProviderFactory.TBAPI_INVALIDATION_EVENTS);
    }
}
