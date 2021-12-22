package ru.alamics.sso.keycloak.event.listener.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;
import org.keycloak.theme.Theme;
import ru.alamics.sso.keycloak.event.listener.factory.SsoEvent;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.settings.SettingsService;

import java.util.HashMap;
import java.util.Map;

import static ru.alamics.sso.settings.SettingConstants.*;

@Slf4j
public class SsoUserCreateEvent extends SsoEvent {

    private static final String BODY_TEMPLATE_CREATE = "mail-account-create.ftl";
    private static final String BODY_TEMPLATE_DATE = "mail-account-data.ftl";

//    private static final String userEnabled = "enabled";
    private AdminEvent event;
    private SettingsService settingsService = null;

    SsoUserCreateEvent (AdminEvent event, KeycloakSession session) {
        super(session);
        settingsService = (SettingsService) Lookup.lookup(SettingsService.class);

        this.event = event;
    }

    @Override
    public void execute () {
        try {
            KeycloakSession session = this.getSession();
            RealmProvider model = session.realms();
            log.info("ExtendedEventListener: admin create user");
            String userId = this.getUserId(event);
            if (userId == null) {
                return;
            }

            RealmModel realm = model.getRealm(event.getRealmId());
            UserModel user = session.users().getUserById(userId, realm);

            if (user != null && user.getEmail() != null) {

                log.info(String.format("realm id %s, %s, %s", event.getRealmId(), realm.getId(), realm.getName()));
                log.info(String.format("user %s, locale %s", user.getId(), session.getContext().resolveLocale(user).toLanguageTag()));
                log.info(String.format("theme %s", session.theme().getTheme(Theme.Type.EMAIL).getName()));

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("userName", user.getUsername());
                attributes.put("userFirstName", user.getFirstName());
                attributes.put("userLastName", user.getLastName());

                attributes.put("emailAccountCreateBodyHtml", settingsService.getSettingsStringValue(EMAIL_CREATE_ACCOUNT,realm.getName()));
                attributes.put("emailAccountDataBodyHtml", settingsService.getSettingsStringValue(EMAIL_DATE_ACCOUNT,realm.getName()));
                attributes.put("phoneInMessage", settingsService.getSettingsStringValue(PHONE_IN_MESSAGE,realm.getName()));
                attributes.put("footerInMassage", settingsService.getSettingsStringValue(FOOTER_IN_MESSAGE,realm.getName()));
                attributes.put("customer", settingsService.getSettingsStringValue(CUSTOMER,realm.getName()));
                attributes.put("gratitudeUp", settingsService.getSettingsStringValue(GRATITUDE_UP,realm.getName()));
                attributes.put("gratitudeDown", settingsService.getSettingsStringValue(GRATITUDE_DOWN,realm.getName()));
                attributes.put("phoneConstLink", settingsService.getSettingsStringValue(PHONE_CONST_LINK,realm.getName()));
                attributes.put("homePage", settingsService.getSettingsStringValue(HOME_PAGE,realm.getName()));

                // если миграция с паролями, просить вводить пароль не нужно
                String subject = settingsService.getSettingsStringValue(ACCOUNT_SUBJECT, realm.getName());
                if (user.isEmailVerified()) {
                    this.sendEmail(user, realm, subject, BODY_TEMPLATE_CREATE, attributes);
                } else {
                    this.sendEmail(user, realm, subject, BODY_TEMPLATE_DATE, attributes);
                }
            } else {
                log.error(String.format("User '%s' not found or do not have email", userId));
            }
        } catch (Exception e) {
            log.error("Error ", e);
        }
    }
}
