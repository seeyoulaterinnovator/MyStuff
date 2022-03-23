package ru.alamics.sso.keycloak.mobile.util;

import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ModelException;
import org.keycloak.sessions.AuthenticationSessionModel;

public class CustomAuthenticationFlowResolver {

    private final static String RESET_CREDENTIALS = "reset_credential";

    public static AuthenticationFlowModel resolveResetCredentialFlow(AuthenticationSessionModel authSession) {
        AuthenticationFlowModel flow = null;
        ClientModel client = authSession.getClient();
        String clientFlow = client.getAuthenticationFlowBindingOverride(RESET_CREDENTIALS);
        if (clientFlow != null) {
            flow = authSession.getRealm().getAuthenticationFlowById(clientFlow);
            if (flow == null) {
                throw new ModelException("Client " + client.getClientId() + " has reset credential flow override, but this flow does not exist");
            }
            return flow;
        }
        return authSession.getRealm().getResetCredentialsFlow();
    }
}
