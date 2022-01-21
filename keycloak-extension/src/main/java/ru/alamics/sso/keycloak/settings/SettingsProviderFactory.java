package ru.alamics.sso.keycloak.settings;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class SettingsProviderFactory implements BaseResourceProviderFactory {

    private static final String PROVIDER_ID = "settings";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new SettingsResourceProvider(session);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
