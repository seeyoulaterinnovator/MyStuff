package ru.alamics.sso.keycloak.auth.link.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class AuthLinkRealmResourceProviderFactory implements BaseResourceProviderFactory {

    private static final String PROVIDER_ID = "auth-link";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {

        return new AuthLinkRealmResourceProvider(session);
    }

}
