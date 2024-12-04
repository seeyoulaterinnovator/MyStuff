package ru.alamics.sso.keycloak.email;

import org.keycloak.Config;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProviderFactory;
import org.keycloak.models.KeycloakSession;

public class SsoEmailTemplateProviderFactory extends FreeMarkerEmailTemplateProviderFactory implements org.keycloak.email.EmailTemplateProviderFactory {
    @Override
    public EmailTemplateProvider create(KeycloakSession session) {
        return new SsoEmailTemplateProvider(session);
    }

    @Override
    public void init(Config.Scope config) {}
}
