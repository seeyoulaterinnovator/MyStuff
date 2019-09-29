package ru.alamics.sso.keycloak.social;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.authenticators.broker.IdpReviewProfileAuthenticator;
import org.keycloak.authentication.authenticators.broker.IdpReviewProfileAuthenticatorFactory;
import org.keycloak.models.KeycloakSession;

public class CustomIdpReviewProfileAuthenticatorFactory extends IdpReviewProfileAuthenticatorFactory {
    private static IdpReviewProfileAuthenticator SINGLETON = new CustomIdpReviewProfileAuthenticator();
    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }
}
