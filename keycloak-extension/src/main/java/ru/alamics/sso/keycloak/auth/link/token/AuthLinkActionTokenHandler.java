package ru.alamics.sso.keycloak.auth.link.token;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.actiontoken.AbstractActionTokenHander;
import org.keycloak.authentication.actiontoken.ActionTokenContext;
import org.keycloak.events.Errors;
import org.keycloak.events.EventType;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.ws.rs.core.Response;
import java.util.Set;

@Slf4j
public class AuthLinkActionTokenHandler extends AbstractActionTokenHander<AuthLinkActionToken> {

    public AuthLinkActionTokenHandler() {
        super(
                AuthLinkActionToken.TOKEN_TYPE,
                AuthLinkActionToken.class,
                Messages.INVALID_REQUEST,
                EventType.EXECUTE_ACTION_TOKEN,
                Errors.INVALID_REQUEST
        );
        log.info("AuthLinkActionTokenHandler");
    }

    @Override
    public Response handleToken(AuthLinkActionToken token, ActionTokenContext<AuthLinkActionToken> tokenContext) {
        // Continue with the authenticator action

        AuthenticationSessionModel authSession = tokenContext.getAuthenticationSession();

        log.info("Handle token for user " + token.getUserId() + ", client = " + authSession.getClient());

        String redirect = null;
        if (authSession.getClient() != null) {
            Set<String> set = authSession.getClient().getRedirectUris();
            for (String validR : set) {

                if (validR == null)
                    continue;

                int idx = validR.indexOf("/*");
                if (idx > -1) {
                    validR = validR.substring(0, idx);
                }

                validR = validR.replaceAll("\\*", "");
                if (validR.length() > 0) {
                    redirect = validR;
                    break;
                }
            }
        }

        String redirectUri = RedirectUtils.verifyRedirectUri(tokenContext.getUriInfo(), redirect,
                tokenContext.getRealm(), authSession.getClient(), false);

        if (redirectUri != null) {
            authSession.setAuthNote(AuthenticationManager.SET_REDIRECT_URI_AFTER_REQUIRED_ACTIONS, "true");

            authSession.setRedirectUri(redirectUri);
            authSession.setClientNote(OIDCLoginProtocol.REDIRECT_URI_PARAM, redirectUri);
        }
        log.info(redirectUri);

        ClientSessionContext clientSessionCtx = AuthenticationProcessor.attachSession(
                authSession,
                null,
                tokenContext.getSession(),
                tokenContext.getRealm(),
                tokenContext.getClientConnection(),
                tokenContext.getEvent());

        return AuthenticationManager.redirectAfterSuccessfulFlow(
                tokenContext.getSession(),
                tokenContext.getRealm(),
                clientSessionCtx.getClientSession().getUserSession(),
                clientSessionCtx,
                tokenContext.getRequest(),
                tokenContext.getUriInfo(),
                tokenContext.getClientConnection(),
                tokenContext.getEvent(),
                authSession);
    }

    @Override
    public boolean canUseTokenRepeatedly(AuthLinkActionToken token, ActionTokenContext<AuthLinkActionToken> tokenContext) {
        return false;
    }
}
