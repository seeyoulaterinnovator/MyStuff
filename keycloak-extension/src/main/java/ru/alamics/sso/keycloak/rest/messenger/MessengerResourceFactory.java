package ru.alamics.sso.keycloak.rest.messenger;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class MessengerResourceFactory implements RealmResourceProviderFactory {

    private static final String PROVIDER_ID = "messenger";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new MessengerResourceProvider(session);
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
