package ru.alamics.sso.keycloak.create.rest;

import org.keycloak.models.KeycloakSession;

import javax.ws.rs.Path;

public class CustomRestResource {

    private KeycloakSession session;


    public CustomRestResource(KeycloakSession session) {
        this.session = session;
    }

    @Path("")
    public CustomUserResource getCustomUserResource() {
        return new CustomUserResource(session);
    }
}