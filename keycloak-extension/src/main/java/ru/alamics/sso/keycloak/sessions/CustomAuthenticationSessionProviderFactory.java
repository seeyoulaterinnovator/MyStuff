package ru.alamics.sso.keycloak.sessions;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.client.hotrod.RemoteCache;
import org.keycloak.Config;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.sessions.infinispan.entities.RootAuthenticationSessionEntity;
import org.keycloak.sessions.AuthenticationSessionProvider;
import org.keycloak.sessions.AuthenticationSessionProviderFactory;

@Slf4j
public class CustomAuthenticationSessionProviderFactory
        implements AuthenticationSessionProviderFactory<AuthenticationSessionProvider> {
    private static final int PROVIDER_PRIORITY = 2;

    private static final String PROVIDER_ID = "custom";

    private static final String AUTH_SESSIONS_LIMIT = "authSessionsLimit";

    private static final int DEFAULT_AUTH_SESSIONS_LIMIT = 300;

    private volatile CustomInfinispanKeyGenerator keyGenerator;

    private volatile RemoteCache<String, RootAuthenticationSessionEntity> authSessionsCache;

    private volatile int authSessionsLimit;

    @Override
    public AuthenticationSessionProvider create(KeycloakSession session) {
        lazyInit(session);
        return new CustomAuthenticationSessionProvider(
                session,
                keyGenerator,
                authSessionsCache,
                authSessionsLimit
        );
    }

    @Override
    public void init(Config.Scope config) {
        int configInt = config.getInt(AUTH_SESSIONS_LIMIT, DEFAULT_AUTH_SESSIONS_LIMIT);
        authSessionsLimit = (configInt <= 0) ? DEFAULT_AUTH_SESSIONS_LIMIT : configInt;
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public int order() {
        return PROVIDER_PRIORITY;
    }

    private void lazyInit(KeycloakSession session) {
        if (authSessionsCache == null) {
            synchronized (this) {
                if (authSessionsCache == null) {
                    InfinispanConnectionProvider connections = session.getProvider(InfinispanConnectionProvider.class);
                    authSessionsCache = connections.getRemoteCache(InfinispanConnectionProvider.AUTHENTICATION_SESSIONS_CACHE_NAME);
                    keyGenerator = new CustomInfinispanKeyGenerator();
                }
            }
        }
    }
}
