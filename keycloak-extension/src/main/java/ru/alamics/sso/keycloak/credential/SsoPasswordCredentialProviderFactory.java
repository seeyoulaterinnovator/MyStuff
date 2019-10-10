package ru.alamics.sso.keycloak.credential;

import org.keycloak.credential.PasswordCredentialProvider;
import org.keycloak.credential.PasswordCredentialProviderFactory;
import org.keycloak.models.KeycloakSession;

public class SsoPasswordCredentialProviderFactory extends PasswordCredentialProviderFactory {

    @Override
    public PasswordCredentialProvider create (KeycloakSession session) {
        return new SsoPasswordCredentialProvider(session);
    }
}
