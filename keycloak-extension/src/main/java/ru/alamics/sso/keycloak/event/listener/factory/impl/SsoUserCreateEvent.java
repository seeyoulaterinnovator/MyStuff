package ru.alamics.sso.keycloak.event.listener.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;
import org.keycloak.theme.Theme;
import ru.alamics.sso.keycloak.event.listener.factory.SsoEvent;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SsoUserCreateEvent extends SsoEvent {

//    private static final String userEnabled = "enabled";
    private AdminEvent event;

    SsoUserCreateEvent (AdminEvent event, KeycloakSession session) {
        super(session);
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

                // если миграция с паролями, просить вводить пароль не нужно
                if (user.isEmailVerified()) {
                    this.sendEmail(user, realm, "emailAccountDataSubject", "mail-account-create.ftl", attributes);
                } else {
                    this.sendEmail(user, realm, "emailAccountDataSubject", "mail-account-data.ftl", attributes);
                }
            } else {
                log.error(String.format("User '%s' not found or do not have email", userId));
            }
        } catch (Exception e) {
            log.error("Error ", e);
        }
    }
}
