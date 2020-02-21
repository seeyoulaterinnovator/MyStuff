package ru.alamics.sso.keycloak.resetcred.impl;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.actiontoken.DefaultActionTokenKey;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.resetcred.ResetCredential;
import ru.alamics.sso.keycloak.resetcred.ResetCredentialEmailOrPhoneFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ResetCredentialEmail extends ResetCredential {


    public ResetCredentialEmail (KeycloakSession session, AuthenticationFlowContext context) {
        super(session, context);
    }

    @Override
    public void reset (UserModel user, String username) {
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        if (user == null) {
            context.forkWithSuccessMessage(new FormMessage(Messages.EMAIL_SENT));
            return;
        }

        String actionTokenUserId = authenticationSession.getAuthNote(DefaultActionTokenKey.ACTION_TOKEN_USER_ID);

        if (actionTokenUserId != null && Objects.equals(user.getId(), actionTokenUserId)) {
            log.debug("Forget-password triggered when reauthenticating user after authentication via action token. Skipping {} screen and using user {} ",  ResetCredentialEmailOrPhoneFactory.ID, user.getUsername());
            context.success();
            return;
        }

        EventBuilder event = context.getEvent();
        // we don't want people guessing usernames, so if there is a problem, just continuously challenge
        if (user.getEmail() == null || user.getEmail().trim().length() == 0) {
            event.user(user)
                    .detail(Details.USERNAME, username)
                    .error(Errors.INVALID_EMAIL);

            context.forkWithSuccessMessage(new FormMessage(Messages.EMAIL_SENT));
            return;
        }

        RealmModel realm = context.getRealm();
        int validityInSecs = realm.getActionTokenGeneratedByUserLifespan(ResetCredentialsActionToken.TOKEN_TYPE);
        int absoluteExpirationInSecs = Time.currentTime() + validityInSecs;

        // We send the secret in the email in a link as a query param.
        String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authenticationSession).getEncodedId();
        ResetCredentialsActionToken token = new ResetCredentialsActionToken(user.getId(), absoluteExpirationInSecs, authSessionEncodedId, authenticationSession.getClient().getClientId());

        String link = UriBuilder
                .fromUri(context.getActionTokenUrl(token.serialize(context.getSession(), realm, context.getUriInfo())))
                .build()
                .toString();

        long expirationInMinutes = TimeUnit.SECONDS.toMinutes(validityInSecs);

        try {
            EmailTemplateProvider template = context.getSession().getProvider(EmailTemplateProvider.class);
            template.setRealm(realm)
                    .setUser(user)
                    .setAuthenticationSession(authenticationSession)
                    .sendPasswordReset(link, expirationInMinutes);

            event.clone().event(EventType.SEND_RESET_PASSWORD)
                    .user(user)
                    .detail(Details.USERNAME, username)
                    .detail(Details.EMAIL, user.getEmail()).detail(Details.CODE_ID, authenticationSession.getParentSession().getId()).success();
            context.forkWithSuccessMessage(new FormMessage(Messages.EMAIL_SENT));
        } catch (EmailException e) {
            event.clone().event(EventType.SEND_RESET_PASSWORD)
                    .detail(Details.USERNAME, username)
                    .user(user)
                    .error(Errors.EMAIL_SEND_FAILED);
            ServicesLogger.LOGGER.failedToSendPwdResetEmail(e);
            Response challenge = context.form()
                    .setError(Messages.EMAIL_SENT_ERROR)
                    .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR, challenge);
        }
    }
}
