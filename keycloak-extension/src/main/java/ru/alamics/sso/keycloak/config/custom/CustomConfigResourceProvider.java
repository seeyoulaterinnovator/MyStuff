package ru.alamics.sso.keycloak.config.custom;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class CustomConfigResourceProvider implements RealmResourceProvider {
    final KeycloakSession session;

    public CustomConfigResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new CustomConfigResource(session);
    }

    @Override
    public void close() {}
}
