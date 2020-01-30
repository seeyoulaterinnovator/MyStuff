package ru.alamics.sso.keycloak.policy;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.policy.PasswordPolicyProvider;
import org.keycloak.policy.PolicyError;

public class MaxLengthPasswordPolicyProvider implements PasswordPolicyProvider {
    private static final String ERROR_MESSAGE = "invalidPasswordMaxLengthMessage";

    private KeycloakSession session;

    public MaxLengthPasswordPolicyProvider (KeycloakSession session) {
        this.session = session;
    }

    @Override
    public PolicyError validate (RealmModel realm, UserModel user, String password) {
        return validate(user.getUsername(), password);
    }

    @Override
    public PolicyError validate (String user, String password) {
        var context = session.getContext();
        int max = context.getRealm().getPasswordPolicy().getPolicyConfig(MaxLengthPasswordPolicyProviderFactory.ID);
        return password.length() > max ? new PolicyError(ERROR_MESSAGE, max) : null;
    }

    @Override
    public Object parseConfig (String value) {
        return parseInteger(value, 16);
    }

    @Override
    public void close () {

    }
}
