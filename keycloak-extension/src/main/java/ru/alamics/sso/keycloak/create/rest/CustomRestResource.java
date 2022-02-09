package ru.alamics.sso.keycloak.create.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;

import javax.ws.rs.Path;

public class CustomRestResource {

    private KeycloakSession session;
    private AdminPermissionEvaluator auth;

    public CustomRestResource(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.auth = auth;
    }

    @Path("")
    public CustomUserResource getCustomUserResource() {
        return new CustomUserResource(session, this.auth);
    }
}