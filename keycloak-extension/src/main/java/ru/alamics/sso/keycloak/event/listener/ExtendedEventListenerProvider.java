package ru.alamics.sso.keycloak.event.listener;

import org.jboss.logging.Logger;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.*;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import javax.ws.rs.core.UriBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


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

        if (event.getOperationType().equals(OperationType.CREATE)
                && event.getResourceType().equals(ResourceType.USER)) {

            // need to send account data to user
            //if (event.getResourcePath() != null && event.getResourcePath().toLowerCase().contains("/reset-password")) {
                try {
                    String[] resPath = event.getResourcePath().split("/");
                    if (resPath.length > 1) {

                        log.info("ExtendedEventListener: admin create user");

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

                                /*

                                // нужен отдельный actionToken, для него flow и отдельная форма смены пароля

                                ClientModel clientModel = session.clientStorageManager().getClientByClientId("account", realm);
                                log.info("got client " + clientModel.toString());

                                RootAuthenticationSessionModel rootAuthenticationSessionModel = session.authenticationSessions().createRootAuthenticationSession(realm);
                                AuthenticationSessionModel authenticationSession = rootAuthenticationSessionModel.createAuthenticationSession(clientModel);
                                log.info("got authenticationSession " + authenticationSession.toString());



                                int validityInSecs = realm.getActionTokenGeneratedByUserLifespan(ResetCredentialsActionToken.TOKEN_TYPE);
                                int absoluteExpirationInSecs = Time.currentTime() + validityInSecs;

                                // We send the secret in the email in a link as a query param.
                                String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authenticationSession).getEncodedId();
                                ResetCredentialsActionToken token = new ResetCredentialsActionToken(
                                        user.getId(), absoluteExpirationInSecs, authSessionEncodedId, authenticationSession.getClient().getClientId());

                                String link = UriBuilder
                                        .fromUri(session.getContext().getActionTokenUrl(token.serialize(session, realm, session.getContext().getUriInfo())))
                                        .build()
                                        .toString();
                                long expirationInMinutes = TimeUnit.SECONDS.toMinutes(validityInSecs);



                                attributes.put("accountLink", link);

                                */

                                emailTemplateProvider
                                    .setRealm(realm)
                                    .setUser(user)
                                    .send(subject, template, attributes);

                                log.info("ExtendedEventListener: admin create user. Account data is sent");

                            } catch (EmailException e) {
                                log.error("Failed to send type mail", e);
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error(e);
                }
            //}
        }
    }

    @Override
    public void close() {
    }
}
