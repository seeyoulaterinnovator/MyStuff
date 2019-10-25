package ru.alamics.sso.keycloak.create.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.registration.service.UserFindService;

import javax.ws.rs.Path;

public class CustomRestResource {

    private KeycloakSession session;
    private UserFindService userFindService;
    private AdminPermissionEvaluator auth;

    public CustomRestResource (KeycloakSession session, UserFindService userFindService, AdminPermissionEvaluator auth) {
        this.session = session;
        this.userFindService = userFindService;
        this.auth = auth;
    }

    @Path("")
    public CustomUserResource getCustomUserResource() {
        return new CustomUserResource(session, this.auth, userFindService);
    }
}