package ru.alamics.sso.jpa.admin;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserProvider;
import org.keycloak.models.UserProviderFactory;

import javax.persistence.EntityManager;

@Slf4j
public class CustomJpaProviderFactory implements UserProviderFactory {

    private static final String PROVIDER_ID = "customjpa";

    @Override
    public UserProvider create(KeycloakSession session) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        return new CustomJpaUserProvider(session, em);
    }

    @Override
    public void init(Config.Scope config) {
        log.info("Creating customjpa");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
