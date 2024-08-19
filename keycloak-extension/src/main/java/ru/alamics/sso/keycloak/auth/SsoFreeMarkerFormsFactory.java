package ru.alamics.sso.keycloak.auth;

import org.keycloak.Config;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.LoginFormsProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class SsoFreeMarkerFormsFactory implements LoginFormsProviderFactory {
    private static final String PROVIDER_ID = "freemarker";

    @Override
    public LoginFormsProvider create(KeycloakSession session) {
        return new SsoFreeMarkerLoginForm(session);
    }

    @Override
    public void init(Config.Scope config) {}

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {}

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}
