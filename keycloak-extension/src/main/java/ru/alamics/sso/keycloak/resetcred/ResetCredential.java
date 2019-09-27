package ru.alamics.sso.keycloak.resetcred;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

public abstract class ResetCredential {
    protected KeycloakSession session;
    protected AuthenticationFlowContext context;

    protected ResetCredential (KeycloakSession session, AuthenticationFlowContext context) {
        this.session = session;
        this.context = context;
    }

    public abstract void reset(UserModel user, String username);
}
