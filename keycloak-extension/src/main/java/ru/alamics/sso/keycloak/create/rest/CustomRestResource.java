package ru.alamics.sso.keycloak.create.rest;

import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.registration.service.UserFindService;

import javax.ws.rs.Path;

public class CustomRestResource {

    private KeycloakSession session;
    private UserFindService userFindService;


    public CustomRestResource(KeycloakSession session, UserFindService userFindService) {
        this.session = session;
        this.userFindService = userFindService;
    }

    @Path("")
    public CustomUserResource getCustomUserResource() {
        return new CustomUserResource(session, userFindService);
    }
}