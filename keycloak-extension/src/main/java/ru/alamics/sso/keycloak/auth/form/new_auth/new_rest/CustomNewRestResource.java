package ru.alamics.sso.keycloak.auth.form.new_auth.new_rest;

import jakarta.ws.rs.Path;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;

public class CustomNewRestResource {

    private KeycloakSession session;
    private AdminPermissionEvaluator auth;

    public CustomNewRestResource(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.auth = auth;
    }

    @Path("")
    public CustomNewResource getCustomUserResource() {
        return new CustomNewResource(session, this.auth);
    }
}
