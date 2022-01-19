package ru.alamics.sso.keycloak.event.listener.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.credential.CredentialModel;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import ru.alamics.sso.keycloak.credential.SsoPasswordCredentialProvider;
import ru.alamics.sso.keycloak.event.listener.factory.SsoEvent;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.model.UserEntityRepresentation;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.alamics.sso.settings.SettingConstants.*;

@Slf4j
public class SsoUserCustomEvent extends SsoEvent {

    private static final String BODY_TEMPLATE_LOGIN_SEND = "mail-login-send.ftl";
    private static final String BODY_TEMPLATE_PASSWORD_RESET_WITH_LOGIN = "mail-password-reset-with-login.ftl";

    private AdminEvent event;

    private SettingsService messageService;

    public SsoUserCustomEvent(AdminEvent event, KeycloakSession session) {
        super(session);
        messageService = (SettingsService) Lookup.lookup(SettingsService.class);
        this.event = event;
    }

    @Override
    public void execute() {
        try {
            String representation = event.getRepresentation();
            if (representation == null || representation.isEmpty()) {
                return;
            }
            KeycloakSession session = this.getSession();
            UserEntityRepresentation userRepresentation = this.getUserEntityRepresentation(representation);
            RealmModel realm = session.realms().getRealm(event.getRealmId());
            String userId = userRepresentation.getId();
            UserModel user = session.users().getUserById(userId, realm);

            if (user != null && user.getEmail() != null) {
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("userName", user.getUsername());
                attributes.put("emailSendLoginBodyHtml", messageService.getSettingsStringValue(EMAIL_SEND_LOGIN_ACCOUNT, realm.getName()));
                attributes.put("emailLoginAndPhoneHtml", messageService.getSettingsStringValue(EMAIL_LOGIN_AND_PHONE_ACCOUNT, realm.getName()));
                attributes.put("emailLoginHtml", messageService.getSettingsStringValue(EMAIL_LOGIN_ACCOUNT, realm.getName()));
                attributes.put("emailPasswordFooterHtml", messageService.getSettingsStringValue(EMAIL_PASSWORD_FOOTER_ACCOUNT, realm.getName()));
                attributes.put("emailResetPasswordBodyHtml", messageService.getSettingsStringValue(EMAIL_RESET_PASSWORD_ACCOUNT, realm.getName()));
                List<String> phones = user.getAttribute("phone");
                if (!phones.isEmpty()) {
                    attributes.put("phone", Util.getFormatNumber(phones.get(0)));
                }
                if (userRepresentation.getRequiredActions().contains(UserEntityRepresentation.SEND_LOGIN)) {
                    this.sendEmail(user, realm, messageService.getSettingsStringValue(ACCOUNT_SUBJECT_SEND_LOGIN, realm.getName()), BODY_TEMPLATE_LOGIN_SEND, attributes);
                } else if (userRepresentation.getRequiredActions().contains(UserEntityRepresentation.SEND_LOGIN_AND_RESET_PASSWORD)) {

                    SsoPasswordCredentialProvider a = new SsoPasswordCredentialProvider(session);
                    a.disableCredentialType(realm, user, CredentialModel.PASSWORD,
                            12 * 60 * 60,
                            messageService.getSettingsStringValue(ACCOUNT_SUBJECT_RESET_PASSWORD, realm.getName()), BODY_TEMPLATE_PASSWORD_RESET_WITH_LOGIN, attributes);
                    //this.sendEmail(user, realm, "emailResetPasswordSubject", "mail-password-reset-with-login.ftl", attributes);
                }
            } else {
                log.error(String.format("User '%s' not found or do not have email", userId));
            }
        } catch (Exception e) {
            log.error("Error ", e);
        }
    }
}
