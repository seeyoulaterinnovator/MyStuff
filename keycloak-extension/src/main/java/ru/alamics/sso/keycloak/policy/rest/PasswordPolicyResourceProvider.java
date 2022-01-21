package ru.alamics.sso.keycloak.policy.rest;

import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

public class PasswordPolicyResourceProvider implements BaseResourceProvider<PasswordPolicyProvider> {

    private final KeycloakSession session;

    public PasswordPolicyResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public PasswordPolicyProvider getResource() {
        return new PasswordPolicyProvider(session);
    }
}
