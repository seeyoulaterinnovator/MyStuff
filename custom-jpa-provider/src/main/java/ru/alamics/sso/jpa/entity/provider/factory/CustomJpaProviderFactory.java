package ru.alamics.sso.jpa.entity.provider.factory;

import org.keycloak.Config;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import ru.alamics.sso.jpa.entity.provider.CustomJpaEntityProvider;

public class CustomJpaProviderFactory implements JpaEntityProviderFactory {
    public static final String PROVIDER_ID = "customJpaEntityProviderFactory";

    @Override
    public JpaEntityProvider create(KeycloakSession session) {
        return new CustomJpaEntityProvider();
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
        return PROVIDER_ID;
    }
}
