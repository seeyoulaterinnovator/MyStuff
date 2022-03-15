package ru.alamics.sso.keycloak.mobile.resource;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class RestFactory implements BaseResourceProviderFactory {

    private static final String PROVIDER_ID = "rest";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new RestProvider(session);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}
