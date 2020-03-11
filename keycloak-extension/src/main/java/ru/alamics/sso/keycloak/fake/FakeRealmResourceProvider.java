package ru.alamics.sso.keycloak.fake;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class FakeRealmResourceProvider implements RealmResourceProvider {

    private KeycloakSession session;

    public FakeRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new FakeRestResource(session);
    }

    @Override
    public void close() {
    }
}
