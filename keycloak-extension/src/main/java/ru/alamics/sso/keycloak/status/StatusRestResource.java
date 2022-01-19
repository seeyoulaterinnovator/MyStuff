package ru.alamics.sso.keycloak.status;

import org.keycloak.models.KeycloakSession;

import javax.ws.rs.Path;

public class StatusRestResource {

    private KeycloakSession session;

    public StatusRestResource(KeycloakSession session) {
        this.session = session;
    }

    @Path("")
    public StatusResource getStatusResource() {
        return new StatusResource(session);
    }
}
