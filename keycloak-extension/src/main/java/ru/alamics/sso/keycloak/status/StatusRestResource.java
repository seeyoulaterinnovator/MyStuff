package ru.alamics.sso.keycloak.status;

import jakarta.ws.rs.Path;
import org.keycloak.models.KeycloakSession;

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
