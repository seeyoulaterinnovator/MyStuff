package ru.alamics.sso.keycloak.userpost.rest;

import org.keycloak.models.KeycloakSession;

import javax.ws.rs.Path;

public class UserPostRestResource {

    private KeycloakSession session;

    public UserPostRestResource(KeycloakSession session) {
        this.session = session;
    }

    @Path("")
    public UserPostResource getAccessResource() {
        return new UserPostResource(session);
    }
}