package ru.alamics.sso.keycloak.event.listener;

import org.keycloak.Config;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class ExtendedEventListenerProviderFactory implements EventListenerProviderFactory {

    @Override
    public ExtendedEventListenerProvider create(KeycloakSession session) {
        EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);
        return new ExtendedEventListenerProvider(session, emailTemplateProvider);
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
        return "ExtendedListener";
    }
}
