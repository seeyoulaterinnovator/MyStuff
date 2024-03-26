package ru.alamics.sso.keycloak.auth.form.new_auth.common_mail_sender;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.actiontoken.verifyemail.VerifyEmailActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.Urls;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.schedule.Translator;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EmailSenderService {

    public static void sendVerifyEmail(KeycloakSession session, LoginFormsProvider forms, UserModel user, AuthenticationSessionModel authSession, EventBuilder event)
            throws UriBuilderException, IllegalArgumentException {
        RealmModel realm = session.getContext().getRealm();
        UriInfo uriInfo = session.getContext().getUri();

        try {
            SettingsService settingsService = Lookup.lookup(SettingsService.class);
            int timeTokenVerifyEmail = settingsService.getSettingsIntValue(SettingConstants.TIME_TOKEN_VERIFY_EMAIL, realm.getName());
            int absoluteExpirationInSecs = Time.currentTime() + timeTokenVerifyEmail;

            String authSessionEncodedId = SsoUtil.generatePattern();
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
//            вызывается при регистрации через сайт и через админку (после перехода по ссылке из первого письма(создан аккаунт))
            log.info("emailTemplateProvider is : " + emailTemplateProvider.getClass());

            emailTemplateProvider.sendVerifyEmail(link, expirationInMinutes);

        } catch (EmailException e) {
            log.error("Failed to send verification email", e);
            event.error(Errors.EMAIL_SEND_FAILED);
        }
    }

//    public static void sendVerifyEmailAdmin(KeycloakSession session, UserModel user, AdminEvent event) throws UriBuilderException, IllegalArgumentException {
//        log.info(" sendVerifyEmailAdmin is called ");
//        RealmModel realm = session.getContext().getRealm();
//        UriInfo uriInfo = session.getContext().getUri();
//
//        try {
//            SettingsService settingsService = Lookup.lookup(SettingsService.class);
//            int timeTokenVerifyEmail = settingsService.getSettingsIntValue(SettingConstants.TIME_TOKEN_VERIFY_EMAIL, realm.getName());
//            int absoluteExpirationInSecs = Time.currentTime() + timeTokenVerifyEmail;
//
//            String authSessionEncodedId = SsoUtil.generatePattern();
//            VerifyEmailActionToken token = new VerifyEmailActionToken(user.getId(), absoluteExpirationInSecs, authSessionEncodedId, user.getEmail(), authSession.getClient().getClientId());
//
//
//            String expirationStrRus = Translator.getRusTranslateTimeUnitBySec(timeTokenVerifyEmail);
//
//            EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class)
//                    .setAuthenticationSession(authSession)
//                    .setRealm(realm)
//                    .setUser(user)
//                    .setAttribute("expTime", expirationStrRus);
////            вызывается при регистрации через сайт и через админку (после перехода по ссылке из первого письма(создан аккаунт))
//            log.info("emailTemplateProvider is : " + emailTemplateProvider.getClass());
//
//            emailTemplateProvider.sendVerifyEmail(link, expirationInMinutes);
//
//        } catch (EmailException e) {
//            log.error("Failed to send verification email", e);
//        }
//    }
}
