package ru.alamics.sso.keycloak.social;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.broker.IdpCreateUserIfUniqueAuthenticatorFactory;
import org.keycloak.models.KeycloakSession;

public class CustomIdpCreateUserIfUniqueAuthenticatorFactory extends IdpCreateUserIfUniqueAuthenticatorFactory {
    private static final String PROVIDER_ID = "idp-create-user-if-unique-custom";
    private static final String DISPLAY_NAME = "Create User If Unique (custom)";

    @Override
    public Authenticator create(KeycloakSession session) {
        return new CustomIdpCreateUserIfUniqueAuthenticator();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
    }
}
