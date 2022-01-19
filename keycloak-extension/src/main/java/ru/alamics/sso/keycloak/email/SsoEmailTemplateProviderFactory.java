package ru.alamics.sso.keycloak.email;

import org.keycloak.Config;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.theme.FreeMarkerUtil;

public class SsoEmailTemplateProviderFactory extends FreeMarkerEmailTemplateProviderFactory implements org.keycloak.email.EmailTemplateProviderFactory {

    private FreeMarkerUtil freeMarker;

    @Override
    public EmailTemplateProvider create(KeycloakSession session) {
        return new SsoEmailTemplateProvider(session, freeMarker);
    }

    @Override
    public void init(Config.Scope config) {
        freeMarker = new FreeMarkerUtil();
    }
}
