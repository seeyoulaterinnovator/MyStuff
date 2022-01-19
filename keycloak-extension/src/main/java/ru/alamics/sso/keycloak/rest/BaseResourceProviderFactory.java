package ru.alamics.sso.keycloak.rest;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public interface BaseResourceProviderFactory extends RealmResourceProviderFactory {

    @Override
    default void init(Config.Scope config) {

    }

    @Override
    default void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    default void close() {

    }
}
