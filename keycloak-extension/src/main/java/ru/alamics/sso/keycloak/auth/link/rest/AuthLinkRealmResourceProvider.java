package ru.alamics.sso.keycloak.auth.link.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

public class AuthLinkRealmResourceProvider implements BaseResourceProvider<AuthLinkResource> {

    private KeycloakSession session;

    public AuthLinkRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public AuthLinkResource getResource() {
        initAuth(this.session);
        return new AuthLinkResource(session);
    }

    @Override
    public void close() {
    }
}
