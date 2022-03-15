package ru.alamics.sso.keycloak.mobile.resource.credentials;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class RestCredentialsFactory implements BaseResourceProviderFactory {

    private static final String PROVIDER_ID = "rest-credentials";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new RestCredentialsProvider(session);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}
