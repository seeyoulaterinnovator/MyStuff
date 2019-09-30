package ru.alamics.sso.keycloak.auth.link.token;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.actiontoken.AbstractActionTokenHander;
import org.keycloak.authentication.actiontoken.ActionTokenContext;
import org.keycloak.events.Errors;
import org.keycloak.events.EventType;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.Response;

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

        log.info("handleToken");
        return tokenContext.processFlow(false, AUTHENTICATE_PATH, tokenContext.getRealm().getBrowserFlow(), null, new AuthenticationProcessor());
    }

    @Override
    public boolean canUseTokenRepeatedly(AuthLinkActionToken token, ActionTokenContext<AuthLinkActionToken> tokenContext) {
        return false;
    }
}
