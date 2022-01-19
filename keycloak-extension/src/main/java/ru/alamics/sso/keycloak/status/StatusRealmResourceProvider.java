package ru.alamics.sso.keycloak.status;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class StatusRealmResourceProvider implements RealmResourceProvider {

    private KeycloakSession session;

    public StatusRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new StatusRestResource(session);
    }

    @Override
    public void close() {
    }
}
