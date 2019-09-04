package ru.alamics.sso.keycloak.access.rest;

import org.keycloak.models.KeycloakSession;

import javax.ws.rs.Path;

public class AccessRestResource {

    private KeycloakSession session;

    public AccessRestResource(KeycloakSession session) {
        this.session = session;
    }

    @Path("")
    public AccessResource getAccessResource() {
        return new AccessResource(session);
    }
}