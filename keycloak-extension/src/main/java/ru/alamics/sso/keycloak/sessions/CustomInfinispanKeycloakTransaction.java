package ru.alamics.sso.keycloak.sessions;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.commons.api.BasicCache;
import org.keycloak.models.KeycloakTransaction;
import org.keycloak.models.sessions.infinispan.InfinispanKeycloakTransaction;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @see InfinispanKeycloakTransaction
 */
@Slf4j
public class CustomInfinispanKeycloakTransaction implements KeycloakTransaction {
    private static final InfinispanKeycloakTransaction.CacheTask TOMBSTONE = new InfinispanKeycloakTransaction.CacheTask() {
        @Override
        public void execute() {}

        @Override
        public String toString() {
            return "Tombstone after removal";
        }
    };

    private boolean active;

    private boolean rollback;

    private final Map<Object, InfinispanKeycloakTransaction.CacheTask> tasks = new LinkedHashMap<>();

    @Override
    public void begin() {
        active = true;
    }

    @Override
    public void commit() {
        if (rollback) {
            throw new RuntimeException("Rollback only!");
        }
        tasks.values().forEach(InfinispanKeycloakTransaction.CacheTask::execute);
    }

    @Override
    public void rollback() {
        tasks.clear();
    }

    @Override
    public void setRollbackOnly() {
        rollback = true;
    }

    @Override
    public boolean getRollbackOnly() {
        return rollback;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public <K, V> void put(BasicCache<K, V> cache, K key, V value, long lifespan, TimeUnit lifespanUnit) {
        log.debug("Adding cache operation: {} on {}", InfinispanKeycloakTransaction.CacheOperation.ADD_WITH_LIFESPAN, key);

        Object taskKey = getTaskKey(cache, key);
        if (tasks.containsKey(taskKey)) {
            throw new IllegalStateException("Can't add session: task in progress for session");
        } else {
            tasks.put(taskKey, new InfinispanKeycloakTransaction.CacheTaskWithValue<V>(value, lifespan, lifespanUnit) {
                @Override
                public void execute() {
                    cache.put(key, value, lifespan, lifespanUnit);
                }

                @Override
                public String toString() {
                    return String.format("CacheTaskWithValue: Operation 'put' for key %s, lifespan %d TimeUnit %s", key, lifespan, lifespanUnit);
                }

                @Override
                public InfinispanKeycloakTransaction.Operation getOperation() {
                    return InfinispanKeycloakTransaction.Operation.PUT;
                }
            });
        }
    }

    public <K, V> void replace(RemoteCache<K, V> cache, K key, V value, long lifespan, TimeUnit lifespanUnit) {
        log.debug("Adding cache operation: {} on {}. Lifespan {} {}.", InfinispanKeycloakTransaction.CacheOperation.REPLACE, key, lifespan, lifespanUnit);

        Object taskKey = getTaskKey(cache, key);
        InfinispanKeycloakTransaction.CacheTask current = tasks.get(taskKey);
        if (current != null) {
            if (current instanceof InfinispanKeycloakTransaction.CacheTaskWithValue) {
                ((InfinispanKeycloakTransaction.CacheTaskWithValue<V>) current).setValue(value);
                ((InfinispanKeycloakTransaction.CacheTaskWithValue<V>) current).updateLifespan(lifespan, lifespanUnit);
            }
        } else {
            tasks.put(taskKey, new InfinispanKeycloakTransaction.CacheTaskWithValue<V>(value, lifespan, lifespanUnit) {
                @Override
                public void execute() {
                    cache.replace(key, value, lifespan, lifespanUnit);
                }

                @Override
                public String toString() {
                    return String.format("CacheTaskWithValue: Operation 'replace' for key %s, lifespan %d TimeUnit %s", key, lifespan, lifespanUnit);
                }
            });
        }
    }

    public <K, V> void remove(BasicCache<K, V> cache, K key) {
        log.debug("Adding cache operation: {} on {}", InfinispanKeycloakTransaction.CacheOperation.REMOVE, key);

        Object taskKey = getTaskKey(cache, key);

        InfinispanKeycloakTransaction.CacheTask current = tasks.get(taskKey);
        if (current != null) {
            if (current instanceof InfinispanKeycloakTransaction.CacheTaskWithValue && ((InfinispanKeycloakTransaction.CacheTaskWithValue<?>) current).getOperation() == InfinispanKeycloakTransaction.Operation.PUT) {
                tasks.put(taskKey, TOMBSTONE);
                return;
            }
            if (current == TOMBSTONE) {
                return;
            }
        }

        tasks.put(taskKey, new InfinispanKeycloakTransaction.CacheTask() {
            @Override
            public void execute() {
                cache.remove(key);
            }

            @Override
            public String toString() {
                return String.format("CacheTask: Operation 'remove' for key %s", key);
            }
        });
    }

    public <K, V> V get(BasicCache<K, V> cache, K key) {
        Object taskKey = getTaskKey(cache, key);
        InfinispanKeycloakTransaction.CacheTask current = tasks.get(taskKey);
        if (current != null) {
            if (current instanceof InfinispanKeycloakTransaction.CacheTaskWithValue) {
                return ((InfinispanKeycloakTransaction.CacheTaskWithValue<V>) current).getValue();
            }
        }
        return cache.get(key);
    }

    private static <K, V> Object getTaskKey(BasicCache<K, V> cache, K key) {
        if (key instanceof String) {
            return cache.getName() + "::" + key;
        } else {
            return key;
        }
    }
}
