package ru.alamics.sso.keycloak.cache;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class CustomCacheResourceProviderFactory implements BaseResourceProviderFactory {
    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new CustomCacheResourceProvider(session);
    }

    @Override
    public String getId() {
        return "custom-cache";
    }
}
