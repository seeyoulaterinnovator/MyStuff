package ru.alamics.sso.keycloak.auth;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public abstract class AuthFactoryBaseClass implements AuthenticatorFactory {

    @Override
    public boolean isConfigurable () {
        return false;
    }

    @Override
    public boolean isUserSetupAllowed () {
        return false;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties () {
        return null;
    }


    @Override
    public void init (Config.Scope config) {

    }

    @Override
    public void postInit (KeycloakSessionFactory factory) {

    }

    @Override
    public String getReferenceCategory () {
        return null;
    }

    @Override
    public void close () {

    }


}
