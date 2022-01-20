package ru.alamics.sso.keycloak.policy;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.policy.PasswordPolicyProvider;
import org.keycloak.policy.PasswordPolicyProviderFactory;

public class MaxLengthPasswordPolicyProviderFactory implements PasswordPolicyProviderFactory {
    protected static final String PROVIDER_ID = "max-length";
    private static final String MAX_PASSWORD_LENGTH = "16";
    private static final String DISPLAY_NAME = "Maximum Length";

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }


    @Override
    public String getConfigType() {
        return PasswordPolicyProvider.INT_CONFIG_TYPE;
    }

    @Override
    public String getDefaultConfigValue() {
        return MAX_PASSWORD_LENGTH;
    }

    @Override
    public boolean isMultiplSupported() {
        return false;
    }

    @Override
    public PasswordPolicyProvider create(KeycloakSession session) {
        return new MaxLengthPasswordPolicyProvider(session);
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
