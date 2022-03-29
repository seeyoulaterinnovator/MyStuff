package ru.alamics.sso.keycloak.cities;

import org.keycloak.models.KeycloakSession;

import javax.ws.rs.Path;

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