package ru.alamics.sso.keycloak.cities;

import jakarta.ws.rs.Path;
import org.keycloak.models.KeycloakSession;

public class CitiesRestResource {

    private KeycloakSession session;

    public CitiesRestResource(KeycloakSession session) {
        this.session = session;
    }

    @Path("")
    public CitiesResource getCitiesResource() {
        return new CitiesResource(session);
    }
}
