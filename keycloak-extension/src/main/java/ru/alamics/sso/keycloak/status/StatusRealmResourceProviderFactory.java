package ru.alamics.sso.keycloak.status;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class StatusRealmResourceProviderFactory implements BaseResourceProviderFactory {

    private static final String PROVIDER_ID = "status";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new StatusRealmResourceProvider(session);
    }

}
