package ru.alamics.sso.keycloak.event.listener.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.credential.CredentialModel;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import ru.alamics.sso.keycloak.credential.SsoPasswordCredentialProvider;
import ru.alamics.sso.keycloak.event.listener.factory.SsoEvent;
import ru.alamics.sso.registration.model.UserEntityRepresentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SsoUserCustomEvent extends SsoEvent {

    private AdminEvent event;

    public SsoUserCustomEvent(AdminEvent event, KeycloakSession session) {
        super(session);
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
                List<String> phones = user.getAttribute("phone");
                if (!phones.isEmpty()) {
                    attributes.put("phone", phones.get(0));
                }
                if (userRepresentation.getRequiredActions().contains(UserEntityRepresentation.SEND_LOGIN)) {
                    this.sendEmail(user, realm, "emailSendLoginSubject", "mail-login-send.ftl", attributes);
                } else if (userRepresentation.getRequiredActions().contains(UserEntityRepresentation.SEND_LOGIN_AND_RESET_PASSWORD)) {

                    SsoPasswordCredentialProvider a = new SsoPasswordCredentialProvider(session);
                    a.disableCredentialType(realm, user, CredentialModel.PASSWORD,
                            12 * 60 * 60,
                            "emailResetPasswordSubject", "mail-password-reset-with-login.ftl", attributes);
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
