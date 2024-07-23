package ru.alamics.sso.keycloak.config.custom;

import org.keycloak.services.resource.RealmResourceProvider;

public class CustomConfigResourceProvider implements RealmResourceProvider {
    @Override
    public Object getResource() {
        return new CustomConfigResource();
    }

    @Override
    public void close() {}
}
