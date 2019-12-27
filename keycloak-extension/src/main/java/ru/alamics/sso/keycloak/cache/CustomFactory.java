package ru.alamics.sso.keycloak.cache;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.connections.infinispan.DefaultInfinispanConnectionProviderFactory;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@Slf4j
public class CustomFactory extends DefaultInfinispanConnectionProviderFactory {
    @Override
    public InfinispanConnectionProvider create(KeycloakSession session) {
        log.info("CustomFactory");
        return super.create(session);
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
}
