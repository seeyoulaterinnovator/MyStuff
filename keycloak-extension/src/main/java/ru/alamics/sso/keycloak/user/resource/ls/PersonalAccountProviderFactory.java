package ru.alamics.sso.keycloak.user.resource.ls;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class PersonalAccountProviderFactory implements BaseResourceProviderFactory {

    private static final String PROVIDER_ID = "personal-account";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new PersonalAccountResourceProvider(session);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
