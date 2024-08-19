package ru.alamics.sso.keycloak.fake;

import jakarta.ws.rs.Path;
import org.keycloak.models.KeycloakSession;

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
