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
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.client.ClientService;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.keycloak.resetcred.ResetCredential;
import ru.alamics.sso.keycloak.resetcred.ResetCredentialEmailOrPhoneFactory;
import ru.alamics.sso.keycloak.util.PhoneFormatter;
import ru.alamics.sso.schedule.Translator;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.text.DecimalFormat;
import java.util.Objects;

@Slf4j
public class ResetCredentialEmail extends ResetCredential {

    private final ClientService clientService;

    private final SettingsService settingsService;

    public ResetCredentialEmail(KeycloakSession session, AuthenticationFlowContext context) {
        super(session, context);
        this.settingsService = Lookup.lookup(SettingsService.class);
        this.clientService = Lookup.lookup(ClientService.class);
    }
//here
    @Override
    public void reset(UserModel user, String username) {
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        if (user == null) {
            context.forkWithSuccessMessage(new FormMessage(Messages.EMAIL_SENT_ERROR));
            return;
        }

        String actionTokenUserId = authenticationSession.getAuthNote(DefaultActionTokenKey.ACTION_TOKEN_USER_ID);

        if (actionTokenUserId != null && Objects.equals(user.getId(), actionTokenUserId)) {
            log.debug("Forget-password triggered when reauthenticating user after authentication via action token. Skipping {} screen and using user {} ", ResetCredentialEmailOrPhoneFactory.PROVIDER_ID, user.getUsername());
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
        int timeTokenResetPass = settingsService.getSettingsIntValue(SettingConstants.TIME_TOKEN_RESET_PASSWORD, realm.getName());
        int absoluteExpirationInSecs = Time.currentTime() + timeTokenResetPass;

        // We send the secret in the email in a link as a query param.
        if (authenticationSession.getRedirectUri().isEmpty()) {
            authenticationSession.setRedirectUri(getRedirectUrl(authenticationSession.getClient()));
        }
        String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authenticationSession).getEncodedId();
        ResetCredentialsActionToken token = new ResetCredentialsActionToken(user.getId(), absoluteExpirationInSecs, authSessionEncodedId, authenticationSession.getClient().getClientId());

        token.setOtherClaims("reduri", getRedirectUrl(authenticationSession.getClient()));

        String link = UriBuilder
                .fromUri(context.getActionTokenUrl(token.serialize(context.getSession(), realm, context.getUriInfo())))
                .build()
                .toString();

        String expirationStrRus = Translator.getRusTranslateTimeUnitBySec(timeTokenResetPass);

        try {
            EmailTemplateProvider template = context.getSession().getProvider(EmailTemplateProvider.class);

            if(UserModelUserMapper.mapToUser(user).getPhone() != null) {
                template.setAttribute("phone", PhoneFormatter.formatPhoneNumber(UserModelUserMapper.mapToUser(user).getPhone().trim()));
                if(username.contains("@") && !user.isEmailVerified()) {
                    user.setEmailVerified(true);
                }
            } else {
                if(!user.isEmailVerified()) {
                    context.forkWithErrorMessage(new FormMessage("Не получается отправить письмо. Учетная запись с такими данными не существует в системе"));
                    return;
                }
            }

            template.setRealm(realm)
                    .setUser(user)
                    .setAuthenticationSession(authenticationSession)
                    .setAttribute("expTime", expirationStrRus)
                    .sendPasswordReset(link, timeTokenResetPass);

            event.clone().event(EventType.SEND_RESET_PASSWORD)
                    .user(user)
                    .detail(Details.USERNAME, username)
                    .detail(Details.EMAIL, user.getEmail())
                    .detail(Details.CODE_ID, authenticationSession.getParentSession().getId())
                    .success();
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

    private String getRedirectUrl(ClientModel client) {

        String redirectUrl = clientService.findMainRedirectUri(client);

        if (redirectUrl != null)
            return redirectUrl;

        return settingsService.getSettingsStringValue(SettingConstants.HOME_PAGE, client.getRealm().getId());
    }
}
