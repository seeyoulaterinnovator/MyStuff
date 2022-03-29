package ru.alamics.sso.keycloak.fake;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class FakeRealmResourceProviderFactory implements BaseResourceProviderFactory {

    private static final String PROVIDER_ID = "fake";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new FakeRealmResourceProvider(session);
    }

}
