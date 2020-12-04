package ru.alamics.sso.keycloak.auth.requiredactions;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.requiredactions.UpdatePassword;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.client.ClientService;
import ru.alamics.sso.keycloak.lookup.Lookup;

@Slf4j
public class SsoUpdatePassword extends UpdatePassword {
    private final static String CLIENT_ID = "lkb2b";
    private final static String DEFAULT_CLIENT_ID = "account";

    @Override
    public void processAction(RequiredActionContext context) {
        super.processAction(context);

        setRedirectAfterAction(context);
    }

    private void setRedirectAfterAction(RequiredActionContext context) {
        final KeycloakSession session = context.getSession();
        final AuthenticationSessionModel currentAuthenticationSession = context.getAuthenticationSession();

        ClientModel client = session.clientStorageManager().getClientByClientId(CLIENT_ID, currentAuthenticationSession.getRealm());
        if (client == null)
            client = session.clientStorageManager().getClientByClientId(DEFAULT_CLIENT_ID, currentAuthenticationSession.getRealm());
        if (client == null) {
            log.error("Redirect after UPDATE_PASSWORD is not setup: clientId={} not found", CLIENT_ID);
            return;
        }

        //т.к. при старте новой сессии задается этот параметр = true, редирект после прохожения всего флоу не происходит
        currentAuthenticationSession.setAuthNote(AuthenticationManager.END_AFTER_REQUIRED_ACTIONS, null);

        currentAuthenticationSession.setAction(AuthenticationSessionModel.Action.AUTHENTICATE.name());
        currentAuthenticationSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);


        final ClientService clientService = (ClientService) Lookup.lookup(ClientService.class);

        if (clientService == null) {
            log.error("ClientService failed lookup. Redirect by clientId={} is not possible", CLIENT_ID);
            return;
        }

        String redirectUri = clientService.findMainRedirectUri(client);

        currentAuthenticationSession.setRedirectUri(redirectUri);
        currentAuthenticationSession.setClientNote(OIDCLoginProtocol.REDIRECT_URI_PARAM, redirectUri);
        currentAuthenticationSession.setClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM, OAuth2Constants.CODE);
        currentAuthenticationSession.setClientNote(OIDCLoginProtocol.ISSUER, Urls.realmIssuer(session.getContext().getUri().getBaseUri(), currentAuthenticationSession.getRealm().getName()));
    }
}
