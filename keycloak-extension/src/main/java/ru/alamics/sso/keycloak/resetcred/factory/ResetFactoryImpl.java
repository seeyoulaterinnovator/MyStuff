package ru.alamics.sso.keycloak.resetcred.factory;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.resetcred.ResetCredential;
import ru.alamics.sso.keycloak.resetcred.impl.ResetCredentialEmail;
import ru.alamics.sso.keycloak.resetcred.type.ResetType;

public class ResetFactoryImpl extends ResetFactory {

    public ResetFactoryImpl(KeycloakSession session, AuthenticationFlowContext context) {
        super(session, context);
    }

    @Override
    public ResetCredential create(ResetType type) {
        ResetCredential credential = null;
        if (type == ResetType.EMAIL) {
            credential = new ResetCredentialEmail(session, context);
        }

        return credential;
    }
}
