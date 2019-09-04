package ru.alamics.sso.keycloak.access.entity.provider.factory;

import org.keycloak.Config;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import ru.alamics.sso.keycloak.access.entity.provider.AccessJpaEntityProvider;

public class AccessJpaProviderFactory implements JpaEntityProviderFactory {
    private static final String ID = "accessJpaEntityProviderFactory";

    @Override
    public JpaEntityProvider create (KeycloakSession session) {
        return new AccessJpaEntityProvider();
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
        return ID;
    }

}
