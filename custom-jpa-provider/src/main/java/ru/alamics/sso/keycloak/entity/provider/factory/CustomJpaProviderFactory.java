package ru.alamics.sso.keycloak.entity.provider.factory;

import org.keycloak.Config;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import ru.alamics.sso.keycloak.entity.provider.CustomJpaEntityProvider;

public class CustomJpaProviderFactory  implements org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory {
    private static final String ID = "customJpaEntityProviderFactory";

    @Override
    public JpaEntityProvider create (KeycloakSession session) {
        return new CustomJpaEntityProvider();
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
