package ru.alamics.sso.keycloak.auth.link.token;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.TokenVerifier;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.actiontoken.AbstractActionTokenHander;
import org.keycloak.authentication.actiontoken.ActionTokenContext;
import org.keycloak.authentication.actiontoken.TokenUtils;
import org.keycloak.authentication.actiontoken.verifyemail.VerifyEmailActionToken;
import org.keycloak.events.Errors;
import org.keycloak.events.EventType;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.ws.rs.core.Response;

import java.util.Objects;

import static org.keycloak.services.resources.LoginActionsService.AUTHENTICATE_PATH;

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

        log.info("Handle token for user " + token.getUserId());

        AuthenticationSessionModel authSession = tokenContext.getAuthenticationSession();

        String redirectUri = RedirectUtils.verifyRedirectUri(tokenContext.getUriInfo(), null,
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

        //log.info(clientSessionCtx.getClientSession().getUserSession().toString());

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
