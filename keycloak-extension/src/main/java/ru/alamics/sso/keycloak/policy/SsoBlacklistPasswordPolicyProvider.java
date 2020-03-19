package ru.alamics.sso.keycloak.policy;

import org.keycloak.models.KeycloakContext;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.policy.BlacklistPasswordPolicyProviderFactory;
import org.keycloak.policy.PasswordPolicyProvider;
import org.keycloak.policy.PolicyError;

import java.nio.file.Path;

public class SsoBlacklistPasswordPolicyProvider implements PasswordPolicyProvider {

    public static final String ERROR_MESSAGE = "invalidPasswordBlacklistedMessage";

    private final KeycloakContext context;

    private final SsoBlacklistPasswordPolicyProviderFactory factory;

    public SsoBlacklistPasswordPolicyProvider(KeycloakContext context, SsoBlacklistPasswordPolicyProviderFactory factory) {
        this.context = context;
        this.factory = factory;
    }

    @Override
    public PolicyError validate(String username, String password) {

        Object policyConfig = context.getRealm().getPasswordPolicy().getPolicyConfig(SsoBlacklistPasswordPolicyProviderFactory.ID);
        if (policyConfig == null) {
            return null;
        }

        if (!(policyConfig instanceof SsoBlacklistPasswordPolicyProviderFactory.PasswordBlacklist)) {
            return null;
        }

        SsoBlacklistPasswordPolicyProviderFactory.PasswordBlacklist blacklist = (SsoBlacklistPasswordPolicyProviderFactory.FileBasedPasswordBlacklist) policyConfig;

        if (!blacklist.contains(password)) {
            return null;
        }

        return new PolicyError(ERROR_MESSAGE);
    }

    @Override
    public PolicyError validate(RealmModel realm, UserModel user, String password) {
        return validate(user.getUsername(), password);
    }

    @Override
    public Object parseConfig(String blacklistName) {

        if (blacklistName == null) {
            return null;
        }

        return factory.resolvePasswordBlacklist(blacklistName);
    }

    @Override
    public void close() {
        //noop
    }

    public static class SsoFileBasedPasswordBlacklist extends BlacklistPasswordPolicyProviderFactory.FileBasedPasswordBlacklist {
        public SsoFileBasedPasswordBlacklist(Path blacklistBasePath, String name) {
            super(blacklistBasePath, name);
        }
    }
}
