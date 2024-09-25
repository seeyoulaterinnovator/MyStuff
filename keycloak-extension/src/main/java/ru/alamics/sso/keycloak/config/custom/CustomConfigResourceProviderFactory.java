package ru.alamics.sso.keycloak.config.custom;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class CustomConfigResourceProviderFactory implements BaseResourceProviderFactory {
    private static final String PROVIDER_ID = "config-custom";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new CustomConfigResourceProvider(session);
    }
}
