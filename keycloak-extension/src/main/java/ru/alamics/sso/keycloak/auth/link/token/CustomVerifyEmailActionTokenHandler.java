package ru.alamics.sso.keycloak.auth.link.token;

import org.keycloak.TokenVerifier;
import org.keycloak.authentication.actiontoken.AbstractActionTokenHander;
import org.keycloak.authentication.actiontoken.ActionTokenContext;
import org.keycloak.authentication.actiontoken.TokenUtils;
import org.keycloak.authentication.actiontoken.verifyemail.VerifyEmailActionToken;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Objects;

public class CustomVerifyEmailActionTokenHandler extends AbstractActionTokenHander<VerifyEmailActionToken> {

    public CustomVerifyEmailActionTokenHandler() {
        super(
                VerifyEmailActionToken.TOKEN_TYPE,
                VerifyEmailActionToken.class,
                Messages.STALE_VERIFY_EMAIL_LINK,
                EventType.VERIFY_EMAIL,
                Errors.INVALID_TOKEN
        );
    }

    @Override
    public TokenVerifier.Predicate<? super VerifyEmailActionToken>[] getVerifiers(ActionTokenContext<VerifyEmailActionToken> tokenContext) {
        return TokenUtils.predicates(
                TokenUtils.checkThat(
                        t -> Objects.equals(t.getEmail(), tokenContext.getAuthenticationSession().getAuthenticatedUser().getEmail()),
                        Errors.INVALID_EMAIL, getDefaultErrorMessage()
                )
        );
    }

    @Override
    public Response handleToken(VerifyEmailActionToken token, ActionTokenContext<VerifyEmailActionToken> tokenContext) {
        UserModel user = tokenContext.getAuthenticationSession().getAuthenticatedUser();
        EventBuilder event = tokenContext.getEvent();

        event.event(EventType.VERIFY_EMAIL).detail(Details.EMAIL, user.getEmail());

        AuthenticationSessionModel authSession = tokenContext.getAuthenticationSession();
        final UriInfo uriInfo = tokenContext.getUriInfo();
        final RealmModel realm = tokenContext.getRealm();
        final KeycloakSession session = tokenContext.getSession();

        if (tokenContext.isAuthenticationSessionFresh()) {
            // Update the authentication session in the token
            token.setCompoundOriginalAuthenticationSessionId(token.getCompoundAuthenticationSessionId());

            String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authSession).getEncodedId();
            token.setCompoundAuthenticationSessionId(authSessionEncodedId);
        }

        // verify user email as we know it is valid as this entry point would never have gotten here.
        //   user.setEmailVerified(true);
        user.removeRequiredAction(UserModel.RequiredAction.VERIFY_EMAIL);
        user.addRequiredAction("phone_verificator_sms");
        user.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD.name());
        authSession.removeRequiredAction(UserModel.RequiredAction.VERIFY_EMAIL);

        authSession.setAuthNote("emailHandler", "emailVerification");

        event.success();

//        if (token.getCompoundOriginalAuthenticationSessionId() != null) {
//            AuthenticationSessionManager asm = new AuthenticationSessionManager(tokenContext.getSession());
//            asm.removeAuthenticationSession(tokenContext.getRealm(), authSession, true);
//
//            return tokenContext.getSession().getProvider(LoginFormsProvider.class)
//                    .setAuthenticationSession(authSession)
//                    .setSuccess(Messages.EMAIL_VERIFIED)
//                    .createInfoPage();
//        }



        tokenContext.setEvent(event.clone().removeDetail(Details.EMAIL).event(EventType.LOGIN));

        String nextAction = AuthenticationManager.nextRequiredAction(session, authSession, tokenContext.getClientConnection(), tokenContext.getRequest(), uriInfo, event);
        return AuthenticationManager.redirectToRequiredActions(session, realm, authSession, uriInfo, nextAction);
    }

    @Override
    public boolean canUseTokenRepeatedly(VerifyEmailActionToken token, ActionTokenContext<VerifyEmailActionToken> tokenContext) {
        return false;
    }
}
