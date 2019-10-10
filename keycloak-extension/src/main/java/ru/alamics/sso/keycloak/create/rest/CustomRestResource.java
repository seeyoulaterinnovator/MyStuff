package ru.alamics.sso.keycloak.create.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.AdminAuth;
import ru.alamics.sso.registration.service.UserFindService;

import javax.ws.rs.Path;

public class CustomRestResource {

    private KeycloakSession session;
    private UserFindService userFindService;
    private AdminAuth auth;

    public CustomRestResource (KeycloakSession session, UserFindService userFindService, AdminAuth auth) {
        this.session = session;
        this.userFindService = userFindService;
        this.auth = auth;
    }

    @Path("")
    public CustomUserResource getCustomUserResource() {
        return new CustomUserResource(session, this.auth, userFindService);
    }
}