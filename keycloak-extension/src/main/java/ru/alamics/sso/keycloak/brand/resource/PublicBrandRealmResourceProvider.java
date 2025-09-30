package ru.alamics.sso.keycloak.brand.resource;

import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

public class PublicBrandRealmResourceProvider implements BaseResourceProvider<PublicBrandResource> {

    private final KeycloakSession session;

    public PublicBrandRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public PublicBrandResource getResource() {
        return new PublicBrandResource(session);
    }

}