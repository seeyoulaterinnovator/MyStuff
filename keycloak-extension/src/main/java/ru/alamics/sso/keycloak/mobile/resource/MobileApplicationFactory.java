package ru.alamics.sso.keycloak.mobile.resource;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class MobileApplicationFactory implements BaseResourceProviderFactory {

    private static final String PROVIDER_ID = "mobile";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new MobileApplicationProvider(session);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}
