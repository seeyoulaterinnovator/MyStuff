package ru.alamics.sso.keycloak.auth.link.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class AuthLinkRealmResourceProvider implements RealmResourceProvider {

    private KeycloakSession session;

    public AuthLinkRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new AuthLinkRestResource(session);
    }

    @Override
    public void close() {
    }
}
