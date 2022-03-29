package ru.alamics.sso.keycloak.event.listener;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class ExtendedEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final String PROVIDER_ID = "ExtendedListener";

    @Override
    public ExtendedEventListenerProvider create(KeycloakSession session) {
        return new ExtendedEventListenerProvider(session);
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
        return PROVIDER_ID;
    }
}
