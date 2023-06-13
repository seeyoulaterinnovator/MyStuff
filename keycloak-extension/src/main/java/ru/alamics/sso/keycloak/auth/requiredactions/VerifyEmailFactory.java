package ru.alamics.sso.keycloak.auth.requiredactions;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.actiontoken.verifyemail.VerifyEmailActionToken;
import org.keycloak.authentication.requiredactions.VerifyEmail;
import org.keycloak.common.util.Time;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.freemarker.beans.ProfileBean;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.Urls;
import org.keycloak.services.validation.Validation;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.theme.Theme;
import org.keycloak.theme.beans.LinkExpirationFormatterMethod;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.schedule.Translator;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static ru.alamics.sso.settings.SettingConstants.ACCOUNT_SUBJECT_VERIFICATION;
import static ru.alamics.sso.settings.SettingConstants.EMAIL_VERIFICATION_LOGIN_ACCOUNT;

@Slf4j
public class VerifyEmailFactory extends VerifyEmail {

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

//        if (context.getUser().isEmailVerified()) {
//            context.success();
//            authSession.removeAuthNote(Constants.VERIFY_EMAIL_KEY);
//            return;
//        }

        String email = context.getUser().getEmail();
        if (Validation.isBlank(email)) {
            context.ignore();
            return;
        }

        LoginFormsProvider loginFormsProvider = context.form();
        Response challenge;

        // Do not allow resending e-mail by simple page refresh, i.e. when e-mail sent, it should be resent properly via email-verification endpoint
        if (!Objects.equals(authSession.getAuthNote(Constants.VERIFY_EMAIL_KEY), email)) {
            authSession.setAuthNote(Constants.VERIFY_EMAIL_KEY, email);
            EventBuilder event = context.getEvent().clone().event(EventType.SEND_VERIFY_EMAIL).detail(Details.EMAIL, email);
            challenge = sendVerifyEmail(context.getSession(), loginFormsProvider, context.getUser(), context.getAuthenticationSession(), event);
        } else {
            challenge = loginFormsProvider.createResponse(UserModel.RequiredAction.VERIFY_EMAIL);
        }

        context.challenge(challenge);
    }

    private Response sendVerifyEmail(KeycloakSession session, LoginFormsProvider forms, UserModel user, AuthenticationSessionModel authSession, EventBuilder event) throws UriBuilderException, IllegalArgumentException {
        RealmModel realm = session.getContext().getRealm();
        UriInfo uriInfo = session.getContext().getUri();

        try {
            SettingsService settingsService = Lookup.lookup(SettingsService.class);
            int timeTokenVerifyEmail = settingsService.getSettingsIntValue(SettingConstants.TIME_TOKEN_VERIFY_EMAIL, realm.getName());
            int absoluteExpirationInSecs = Time.currentTime() + timeTokenVerifyEmail;
            String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authSession).getEncodedId();
            VerifyEmailActionToken token = new VerifyEmailActionToken(user.getId(), absoluteExpirationInSecs, authSessionEncodedId, user.getEmail(), authSession.getClient().getClientId());
            UriBuilder builder = Urls.actionTokenBuilder(uriInfo.getBaseUri(), token.serialize(session, realm, uriInfo),
                    authSession.getClient().getClientId(), authSession.getTabId());
            String link = builder.build(realm.getName()).toString();
            long expirationInMinutes = TimeUnit.SECONDS.toMinutes(timeTokenVerifyEmail);

            String expirationStrRus = Translator.getRusTranslateTimeUnitBySec(timeTokenVerifyEmail);

            EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class)
                    .setAuthenticationSession(authSession)
                    .setRealm(realm)
                    .setUser(user)
                    .setAttribute("expTime", expirationStrRus);

            if (user.isEmailVerified()) {
                sendAuthorizationEmail(emailTemplateProvider, user, link, timeTokenVerifyEmail, session, realm.getName());
            } else {
                emailTemplateProvider.sendVerifyEmail(link, expirationInMinutes);
            }
            event.success();
        } catch (EmailException e) {
            log.error("Failed to send verification email", e);
            event.error(Errors.EMAIL_SEND_FAILED);
        }

        return forms.setAttribute("mail", user.getEmail()).createResponse(UserModel.RequiredAction.VERIFY_EMAIL);
    }

    private void sendAuthorizationEmail(EmailTemplateProvider emailTemplateProvider, UserModel user, String link,
                                        int validityInSecs, KeycloakSession session, String reamName) throws EmailException {

        long expirationInMinutes = TimeUnit.SECONDS.toMinutes(validityInSecs);
        String expirationStrRus = Translator.getRusTranslateTimeUnitBySec(validityInSecs);

        SettingsService settingsService = Lookup.lookup(SettingsService.class);

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("user", new ProfileBean(user));
        attributes.put("link", link);
        attributes.put("linkExpiration", expirationInMinutes);
        attributes.put("expTime", expirationStrRus);
        attributes.put("emailVerificationLoginBodyHtml", settingsService.getSettingsStringValue(EMAIL_VERIFICATION_LOGIN_ACCOUNT, reamName));

        try {
            Locale locale = session.getContext().resolveLocale(user);
            attributes.put("linkExpirationFormatter",
                    new LinkExpirationFormatterMethod(session.theme().getTheme(Theme.Type.EMAIL).getMessages(locale), locale));
        } catch (IOException e) {
            throw new EmailException("Failed to template email", e);
        }

        emailTemplateProvider.send(settingsService.getSettingsStringValue(ACCOUNT_SUBJECT_VERIFICATION, reamName),
                "email-verification-login.ftl", attributes);
    }
}
