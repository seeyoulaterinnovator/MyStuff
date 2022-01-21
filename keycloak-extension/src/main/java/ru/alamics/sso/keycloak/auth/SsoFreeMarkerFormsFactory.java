package ru.alamics.sso.keycloak.auth;

import org.keycloak.Config;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.LoginFormsProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.theme.FreeMarkerUtil;

public class SsoFreeMarkerFormsFactory implements LoginFormsProviderFactory {
    private static final String PROVIDER_ID = "freemarker";

    private FreeMarkerUtil freeMarker;

    @Override
    public LoginFormsProvider create(KeycloakSession session) {
        return new SsoFreeMarkerLoginForm(session, freeMarker);
    }

    @Override
    public void init(Config.Scope config) {
        freeMarker = new FreeMarkerUtil();
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
        freeMarker = null;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}
