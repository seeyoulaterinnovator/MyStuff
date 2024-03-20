package ru.alamics.sso.keycloak.event.listener.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.actiontoken.verifyemail.VerifyEmailActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.freemarker.beans.ProfileBean;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.AuthenticationSessionProvider;
import org.keycloak.theme.Theme;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.theme.beans.LinkExpirationFormatterMethod;
import ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil;
import ru.alamics.sso.keycloak.event.listener.factory.SsoEvent;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.schedule.Translator;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.Util;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.keycloak.exportimport.ExportImportConfig.getRealmName;
import static ru.alamics.sso.keycloak.auth.form.new_auth.common_mail_sender.EmailSenderService.sendVerifyEmail;
import static ru.alamics.sso.settings.SettingConstants.*;

@Slf4j
public class SsoUserCreateEvent extends SsoEvent {

    private static final String BODY_TEMPLATE_CREATE = "mail-account-create.ftl";
    private static final String BODY_TEMPLATE_DATE = "mail-account-data.ftl";
    private static final String  BODY_TEMPLATE_EMAIL_VERIFICATION = "email-verification.ftl";
    private static final String BLANK_PAGE = "blank-page.ftl";

    //    private static final String userEnabled = "enabled";
    private AdminEvent event;
    private SettingsService settingsService = null;

    SsoUserCreateEvent(AdminEvent event, KeycloakSession session) {
        super(session);
        settingsService = Lookup.lookup(SettingsService.class);

        this.event = event;
    }

    @Override
    public void execute() {
        try {
            KeycloakSession session = this.getSession();
            RealmProvider model = session.realms();
            log.info("ExtendedEventListener: admin create user");
            String userId = this.getUserId(event);
            RealmModel realm = model.getRealm(event.getRealmId());
            SettingsService settingsService = Lookup.lookup(SettingsService.class);
            int timeTokenVerifyEmail = settingsService.getSettingsIntValue(SettingConstants.TIME_TOKEN_VERIFY_EMAIL, realm.getName());
            int absoluteExpirationInSecs = Time.currentTime() + timeTokenVerifyEmail;
            UriInfo uriInfo = session.getContext().getUri();
            UserModel userModel = session.users().getUserById(userId, realm);


            if (userId == null) {
                return;
            }




            if (userModel != null && userModel.getEmail() != null) {

                log.info(String.format("realm id %s, %s, %s", event.getRealmId(), realm.getId(), realm.getName()));
                log.info(String.format("user %s, locale %s", userModel.getId(), session.getContext().resolveLocale(userModel).toLanguageTag()));
                log.info(String.format("theme %s", session.theme().getTheme(Theme.Type.EMAIL).getName()));

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("userName", userModel.getUsername());
                attributes.put("userFirstName", userModel.getFirstName());
                attributes.put("userLastName", userModel.getLastName());

                List<String> phones = userModel.getAttribute("phone");
                if (!phones.isEmpty() && phones.get(0).length() == 11) {
                    attributes.put("phone", Util.getFormatNumber(phones.get(0)));
                }

                attributes.put("emailAccountCreateBodyHtml", settingsService.getSettingsStringValue(EMAIL_CREATE_ACCOUNT, realm.getName()));
                attributes.put("linkPassword", settingsService.getSettingsStringValue(EMAIL_LINK_PASSWORD, realm.getName()));

                attributes.put("emailAccountDataBodyHtml", settingsService.getSettingsStringValue(EMAIL_DATE_ACCOUNT, realm.getName()));
                attributes.put("emailLoginAndPhoneHtml", settingsService.getSettingsStringValue(EMAIL_LOGIN_AND_PHONE_ACCOUNT, realm.getName()));
                attributes.put("emailLoginHtml", settingsService.getSettingsStringValue(EMAIL_LOGIN_ACCOUNT, realm.getName()));

                attributes.put("phoneInMessage", settingsService.getSettingsStringValue(PHONE_IN_MESSAGE, realm.getName()));
                attributes.put("footerInMassage", settingsService.getSettingsStringValue(FOOTER_IN_MESSAGE, realm.getName()));
                attributes.put("customer", settingsService.getSettingsStringValue(CUSTOMER, realm.getName()));
                attributes.put("gratitudeUp", settingsService.getSettingsStringValue(GRATITUDE_UP, realm.getName()));
                attributes.put("gratitudeDown", settingsService.getSettingsStringValue(GRATITUDE_DOWN, realm.getName()));
                attributes.put("phoneConstLink", settingsService.getSettingsStringValue(PHONE_CONST_LINK, realm.getName()));
                attributes.put("homePage", settingsService.getSettingsStringValue(HOME_PAGE, realm.getName()));
                attributes.put("email", userModel.getEmail());

                // если миграция с паролями, просить вводить пароль не нужно
//                String subject = settingsService.getSettingsStringValue(ACCOUNT_SUBJECT, realm.getName());
//                if (userModel.isEmailVerified()) {
//                    this.sendEmail(userModel, realm, subject, BODY_TEMPLATE_CREATE, attributes);
//                } else {
//                    this.sendEmail(userModel, realm, subject, BODY_TEMPLATE_DATE, attributes);
//                }

//                String expirationStrRus = Translator.getRusTranslateTimeUnitBySec(timeTokenVerifyEmail);
//

//                String clientId = session.getContext().getClient().getClientId();
//                ClientModel client = session.clientStorageManager().getClientByClientId(clientId, realm);
//                String tabId = "tabId";
//                встать в дебаге и понять кто может иметь отношение к AuthenticationSessionModel, какие сессии существуют
//                AuthenticationSessionModel authSession = (AuthenticationSessionModel) session.authenticationSessions().
//                        getRootAuthenticationSession(realm, tabId).getAuthenticationSessions();




//                AuthenticationSessionModel authSession = session.authenticationSessions().createA;
//                EmailTemplateProvider emailTemplateProvider = (EmailTemplateProvider) this;


                EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);
                emailTemplateProvider.setRealm(session.getContext().getRealm());
                emailTemplateProvider.setUser(session.users().getUserById(userModel.getId(), session.getContext().getRealm()));
                long expirationInMinutes = TimeUnit.SECONDS.toMinutes(timeTokenVerifyEmail);

                emailTemplateProvider.sendVerifyEmail(settingsService.getSettingsStringValue(EMAIL_LINK_PASSWORD, realm.getName()), expirationInMinutes);


            } else {
                log.error(String.format("User '%s' not found or do not have email", userId));
            }
        } catch (Exception e) {
            log.error("Error ", e);
        }

    }
//    private Response createForm(RequiredActionContext context) {
//        LoginFormsProvider form = context.form();
//        return form.createForm(BLANK_PAGE);
//    }
}
