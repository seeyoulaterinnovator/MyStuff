package ru.alamics.sso.keycloak.fake;

import org.keycloak.models.KeycloakSession;

import javax.ws.rs.Path;

public class FakeRestResource {

    private KeycloakSession session;

    public FakeRestResource(KeycloakSession session) {
        this.session = session;
    }

    @Path("")
    public FakeResource getFakeResource() {
        return new FakeResource(session);
    }
}
