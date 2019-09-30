package ru.alamics.sso.keycloak.auth.link.rest;

import org.keycloak.models.KeycloakSession;

import javax.ws.rs.Path;

public class AuthLinkRestResource {

    private KeycloakSession session;

    public AuthLinkRestResource(KeycloakSession session) {
        this.session = session;
    }

    @Path("")
    public AuthLinkResource getAuthLinkResource() {
        return new AuthLinkResource(session);
    }
}
