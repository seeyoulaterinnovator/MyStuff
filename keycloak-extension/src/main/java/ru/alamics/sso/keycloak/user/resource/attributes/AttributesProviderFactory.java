package ru.alamics.sso.keycloak.user.resource.attributes;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class AttributesProviderFactory implements BaseResourceProviderFactory{
    private static final String ID = "user-attributes";

    @Override
    public RealmResourceProvider create (KeycloakSession session) {
        return new AttributesResourceProvider(session);
    }

    @Override
    public String getId () {
        return ID;
    }
}
