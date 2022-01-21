package ru.alamics.sso.keycloak.resetcred.factory;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.resetcred.ResetCredential;
import ru.alamics.sso.keycloak.resetcred.type.ResetType;

public abstract class ResetFactory {
    protected KeycloakSession session;
    protected AuthenticationFlowContext context;

    ResetFactory(KeycloakSession session, AuthenticationFlowContext context) {
        this.session = session;
        this.context = context;
    }

    public abstract ResetCredential create(ResetType type);
}
