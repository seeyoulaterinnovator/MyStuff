package ru.alamics.sso.keycloak.event.listener.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.email.EmailException;
import org.keycloak.email.freemarker.beans.ProfileBean;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.theme.Theme;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.theme.beans.LinkExpirationFormatterMethod;
import ru.alamics.sso.keycloak.event.listener.factory.SsoEvent;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.keycloak.exportimport.ExportImportConfig.getRealmName;
import static ru.alamics.sso.settings.SettingConstants.*;

@Slf4j
public class SsoUserCreateEvent extends SsoEvent {

    private static final String BODY_TEMPLATE_CREATE = "mail-account-create.ftl";
    private static final String BODY_TEMPLATE_DATE = "mail-account-data.ftl";
    private static final String  BODY_TEMPLATE_EMAIL_VERIFICATION = "email-verification.ftl";

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
            if (userId == null) {
                return;
            }

            RealmModel realm = model.getRealm(event.getRealmId());
            UserModel userModel = session.users().getUserById(userId, realm);

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

//                attributes.put("user", new ProfileBean(user));
//                addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

//                attributes.put("realmName", realm);
                attributes.put("emailVerificationBodyHtml", settingsService.getSettingsStringValue(SettingConstants.EMAIL_VERIFICATION_ACCOUNT, realm.getName()));

                // если миграция с паролями, просить вводить пароль не нужно
                String subject = settingsService.getSettingsStringValue(ACCOUNT_SUBJECT_VERIFICATION, realm.getName());
//                this.sendEmail(userModel, realm, subject, BODY_TEMPLATE_EMAIL_VERIFICATION, attributes);
//                if (userModel.isEmailVerified()) {
//                    log.info("sendEmail(userModel, realm, subject, BODY_TEMPLATE_CREATE, attributes)");
//                    this.sendEmail(userModel, realm, subject, BODY_TEMPLATE_EMAIL_VERIFICATION, attributes);
//                } else {
//                    log.info("sendEmail(userModel, realm, subject, BODY_TEMPLATE_DATE, attributes)");
//                    this.sendEmail(userModel, realm, subject, BODY_TEMPLATE_EMAIL_VERIFICATION, attributes);
//                }

//                if (userModel.isEmailVerified() && !userModel.getRequiredActions().contains("email_sender")) {
//                    log.info("sendEmail(userModel, realm, subject, BODY_TEMPLATE_CREATE, attributes)");
//                    this.sendEmail(userModel, realm, subject, BODY_TEMPLATE_CREATE, attributes);
//                } else if (!userModel.isEmailVerified() && !userModel.getRequiredActions().contains("email_sender")) {
//                    log.info("sendEmail(userModel, realm, subject, BODY_TEMPLATE_DATE, attributes)");
//                    this.sendEmail(userModel, realm, subject, BODY_TEMPLATE_DATE, attributes);
//                } else if (userModel.isEmailVerified() && userModel.getAttribute("phone").size() == 0) {
//                    log.info("New check !! sendEmail(userModel, realm, subject, BODY_TEMPLATE_CREATE, attributes)");
//                    this.sendEmail(userModel, realm, subject, BODY_TEMPLATE_CREATE, attributes);
//                } else if (!userModel.isEmailVerified() && userModel.getAttribute("phone").size() == 0) {
//                    log.info("New check !! sendEmail(userModel, realm, subject, BODY_TEMPLATE_DATE, attributes)");
//                    this.sendEmail(userModel, realm, subject, BODY_TEMPLATE_DATE, attributes);
//                }
            } else {
                log.error(String.format("User '%s' not found or do not have email", userId));
            }
        } catch (Exception e) {
            log.error("Error ", e);
        }

    }
//    protected void addLinkInfoIntoAttributes(String link, long expirationInMinutes, Map<String, Object> attributes) throws EmailException {
//        attributes.put("link", link);
//        attributes.put("linkExpiration", expirationInMinutes);
//        try {
//            Locale locale = session.getContext().resolveLocale(user);
//            attributes.put("linkExpirationFormatter", new LinkExpirationFormatterMethod(getTheme().getMessages(locale), locale));
//        } catch (IOException e) {
//            throw new EmailException("Failed to template email", e);
//        }
//    }
}
