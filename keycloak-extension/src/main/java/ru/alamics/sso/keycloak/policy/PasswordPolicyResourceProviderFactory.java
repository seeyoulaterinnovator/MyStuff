package ru.alamics.sso.keycloak.policy;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class PasswordPolicyResourceProviderFactory implements BaseResourceProviderFactory {
    private final static String ID = "password-policy";

    @Override
    public RealmResourceProvider create (KeycloakSession session) {
        return new PasswordPolicyResourceProvider(session);
    }

    @Override
    public String getId () {
        return ID;
    }
}
