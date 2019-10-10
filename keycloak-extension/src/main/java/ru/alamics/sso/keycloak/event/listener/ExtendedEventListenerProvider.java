package ru.alamics.sso.keycloak.event.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.events.jpa.AdminEventEntity;
import org.keycloak.models.*;
import org.keycloak.services.Urls;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.keycloak.theme.Theme;
import ru.alamics.sso.registration.model.UserEntityRepresentation;

import javax.persistence.EntityManager;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExtendedEventListenerProvider implements EventListenerProvider {

    private static final String userEnabled = "enabled";
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
            onEventCreate(event);
        }

        if (event.getOperationType().equals(OperationType.UPDATE)
                && event.getResourceType().equals(ResourceType.USER)) {
            onEventUpdate(event);
        }
    }

    @Override
    public void close() {
    }

    private String getUserId(AdminEvent event) {
        String[] resPath = event.getResourcePath().split("/");

        String userId = null;
        for (String part : resPath) {
            if (part.length() == 36) {
                userId = part;
                break;
            }
        }
        return userId;
    }

    private void onEventUpdate(AdminEvent event) {
        try {
            UserEntityRepresentation userNow = getUserEntityRepresentation(event.getRepresentation());
            if (!userNow.isEnabled()){
                return;
            }
            String userId = getUserId(event);
            if (userId == null) {
                return;
            }
            AdminEventEntity adminEventEntity = findAdminEvent(userId);
            if (adminEventEntity == null){
                return;
            }
            UserEntityRepresentation userLast = getUserEntityRepresentation(adminEventEntity.getRepresentation());
            if ((userNow.isEnabled() == userLast.isEnabled())){
                return;
            }

            log.info("ExtendedEventListener: admin update user");

            RealmModel realm = model.getRealm(event.getRealmId());
            UserModel user = session.users().getUserById(userId, realm);

            if (user == null || user.getEmail() == null) {
                log.error(String.format("User '%s' not found or do not have email", userId));
            }

            Map<String, Object> attributes = new HashMap<String, Object>();
            attributes.put("userName", user.getUsername());
            attributes.put("userFirstName", user.getFirstName());
            attributes.put("userLastName", user.getLastName());
            sendEmail(user, realm, "emailEnabledAccountSubject", "mail-enabled-account.ftl", attributes);
        } catch (Exception e) {
            log.error("Error ", e);
        }
    }

    private AdminEventEntity findAdminEvent(String userId) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        List<AdminEventEntity> adminEventEntities = em.createQuery("" +
                "select ae " +
                "from AdminEventEntity ae " +
                "where ae.representation like concat('%', :userId, '%') " +
                "order by ae.time DESC ", AdminEventEntity.class)
                .setParameter("userId", userId)
                .getResultList();
        if (adminEventEntities == null || adminEventEntities.isEmpty() || adminEventEntities.size() == 1){
            return null;
        }

        return adminEventEntities.get(1);
    }

    private UserEntityRepresentation getUserEntityRepresentation(String representation) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(representation, UserEntityRepresentation.class);
    }

    private void onEventCreate(AdminEvent event) {
        // need to send account data to user
        try {
            log.info("ExtendedEventListener: admin create user");

            String userId = getUserId(event);
            if (userId == null) {
                return;
            }

            RealmModel realm = model.getRealm(event.getRealmId());
            UserModel user = session.users().getUserById(userId, realm);

            if (user != null && user.getEmail() != null) {

                log.info(String.format("realm id %s, %s, %s", event.getRealmId(), realm.getId(), realm.getName()));
                log.info(String.format("user %s, locale %s", user.getId(), session.getContext().resolveLocale(user).toLanguageTag()));
                log.info(String.format("theme %s", session.theme().getTheme(Theme.Type.EMAIL).getName()));

                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("userName", user.getUsername());
                attributes.put("userFirstName", user.getFirstName());
                attributes.put("userLastName", user.getLastName());

                sendEmail(user, realm, "emailAccountDataSubject", "mail-account-data.ftl", attributes);
            } else {
                log.error(String.format("User '%s' not found or do not have email", userId));
            }
        } catch (Exception e) {
            log.error("Error ", e);
        }
    }

    private void sendEmail(UserModel user, RealmModel realm, String subject, String template, Map<String, Object> attributes){
        try {
            log.info("send to " + user.getEmail());

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

            UriInfo uriInfo = session.getContext().getUri();

            UriBuilder builder = Urls.actionTokenBuilder(uriInfo.getBaseUri(), token.serialize(session, realm, uriInfo),
                    "account", "");
            String link = builder.build(realm.getName()).toString();
            attributes.put("accountLink", link);

            emailTemplateProvider
                    .setRealm(realm)
                    .setUser(user)
                    .send(subject, template, attributes);

            log.info("ExtendedEventListener: admin create user. Account data is sent");

            Map<String, Object> repr = new HashMap<String, Object>();
            repr.put("action", "send_account_data");
            repr.put("email", user.getEmail());

            AdminAuth adminAuth = new AdminAuth(realm, null, user, clientModel);

            new AdminEventBuilder(realm, adminAuth, session, session.getContext().getConnection())
                    .realm(realm)
                    .resource(ResourceType.USER)
                    .operation(OperationType.ACTION)
                    .representation(repr)
                    .resourcePath(session.getContext().getUri(), user.getId())
                    .success();

        } catch (EmailException e) {
            log.error("Failed to send type mail", e);
        }
    }

}
