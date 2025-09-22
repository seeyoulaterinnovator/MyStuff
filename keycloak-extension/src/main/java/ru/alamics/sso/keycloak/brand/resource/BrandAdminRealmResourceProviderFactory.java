package ru.alamics.sso.keycloak.brand.resource;

import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class BrandAdminRealmResourceProviderFactory implements BaseResourceProviderFactory {

    public static final String ID = "brands";

    @Override
    public BrandAdminRealmResourceProvider create(KeycloakSession session) {
        return new BrandAdminRealmResourceProvider(session);
    }

    @Override
    public String getId() {
        return ID;
    }
}