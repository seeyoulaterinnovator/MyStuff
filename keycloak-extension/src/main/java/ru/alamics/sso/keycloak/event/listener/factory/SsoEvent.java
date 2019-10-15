package ru.alamics.sso.keycloak.event.listener.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.Urls;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import ru.alamics.sso.registration.model.UserEntityRepresentation;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class SsoEvent {
    private final KeycloakSession session;

    public SsoEvent (KeycloakSession session) {
        this.session = session;
    }

    public abstract void execute ();

    protected void sendEmail(UserModel user, RealmModel realm, String subject, String template, Map<String, Object> attributes){
        try {
            log.info("send to " + user.getEmail());
            var emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);
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

            emailTemplateProvider.setRealm(realm)
                    .setUser(user)
                    .send(subject, template, attributes);

            log.info("ExtendedEventListener: admin create user. Account data is sent");

            Map<String, Object> repr = new HashMap<>();
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

    protected UserEntityRepresentation getUserEntityRepresentation (String representation) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(representation, UserEntityRepresentation.class);
    }

    public KeycloakSession getSession () {
        return session;
    }

    public String getUserId(AdminEvent event) {
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
}
