package ru.alamics.sso.keycloak.auth.requiredactions;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.models.KeycloakSessionFactory;

public abstract class AbstractRequiredActionFactory implements RequiredActionFactory {

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
