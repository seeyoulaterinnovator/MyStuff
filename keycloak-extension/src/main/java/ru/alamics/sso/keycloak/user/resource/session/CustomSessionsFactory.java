package ru.alamics.sso.keycloak.user.resource.session;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class CustomSessionsFactory implements BaseResourceProviderFactory {
    private static final String PROVIDER_ID = "sessions";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new CustomSessionsProvider(session);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
