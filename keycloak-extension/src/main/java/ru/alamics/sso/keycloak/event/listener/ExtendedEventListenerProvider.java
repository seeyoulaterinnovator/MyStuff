package ru.alamics.sso.keycloak.event.listener;

import org.jboss.logging.Logger;
import org.keycloak.credential.CredentialModel;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExtendedEventListenerProvider implements EventListenerProvider {

    private static final Logger log = Logger.getLogger(ExtendedEventListenerProvider.class);

    private KeycloakSession session;
    private RealmProvider model;
    private EmailTemplateProvider emailTemplateProvider;

    public ExtendedEventListenerProvider(KeycloakSession session, EmailTemplateProvider emailTemplateProvider) {
        this.session = session;
        this.model = session.realms();
        this.emailTemplateProvider = emailTemplateProvider;
    }

    @Override
    public void onEvent(Event event) {

    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {

        if (event.getOperationType().equals(OperationType.ACTION)
                && event.getResourceType().equals(ResourceType.USER)) {

            // need to send account data to user
            if (event.getResourcePath() != null && event.getResourcePath().toLowerCase().contains("/reset-password")) {
                try {
                    String[] resPath = event.getResourcePath().split("/");
                    if (resPath.length > 1) {

                        log.info("ExtendedEventListener: reset-password");

                        RealmModel realm = model.getRealm(event.getRealmId());
                        UserModel user = session.users().getUserById(resPath[1], realm);

                        List<CredentialModel> cred = session.userCredentialManager().getStoredCredentialsByType(realm, user, CredentialModel.PASSWORD);

                        if (user != null && user.getEmail() != null) {
                            try {
                                String subject = "emailAccountDataSubject";
                                String template = "mail-account-data.ftl";

                                Map<String, Object> attributes = new HashMap<String, Object>();
                                attributes.put("userName", user.getUsername());
                                attributes.put("userFirstName", user.getFirstName());
                                attributes.put("userLastName", user.getLastName());

                                if (cred != null && cred.size() > 0)
                                    attributes.put("password", cred.get(0).getValue());

                                attributes.put("accountLink", "http://domru-sso.alamics.ru/auth/realms/user/account/"); // TODO


                                emailTemplateProvider
                                    .setRealm(realm)
                                    .setUser(user).send(subject, template, attributes);

                                log.info("ExtendedEventListener: reset-password. Account data is sent");

                            } catch (EmailException e) {
                                log.error("Failed to send type mail", e);
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }

    @Override
    public void close() {
    }
}
