package ru.alamics.sso.keycloak.brand.resource;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class BrandRealmResourceProviderFactory implements BaseResourceProviderFactory {

    private static final String PROVIDER_ID = "brands";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new BrandRealmResourceProvider(session);
    }
}