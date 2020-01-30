package ru.alamics.sso.keycloak.event.listener.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.keycloak.theme.Theme;
import ru.alamics.sso.emailer.EmailModel;
import ru.alamics.sso.emailer.EmailSender;
import ru.alamics.sso.registration.model.UserEntityRepresentation;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Slf4j
public abstract class SsoEvent {
    private final static String CLIENT_ID = "lkb2b";

    private final KeycloakSession session;
    private final EmailSender emailSender;

    public SsoEvent(KeycloakSession session) {
        this.session = session;
        try {
            this.emailSender = (EmailSender) new InitialContext().lookup("java:global/domru-sso/" + EmailSender.class.getSimpleName());
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
    }

    public abstract void execute();

    protected void sendEmail(UserModel user, RealmModel realm, String subject, String template, Map<String, Object> attributes) {
        try {
            ClientModel clientModel = session.clientStorageManager().getClientByClientId(CLIENT_ID, realm);
            if (clientModel == null) {
                log.error("Failed to send email: {}", "not client=\"" + CLIENT_ID + "\" to redirect!");
                return;
            }

            AuthenticationSessionModel authenticationSession = createAuthenticationSessionForClient(realm, clientModel);

            int validityInSecs = realm.getActionTokenGeneratedByUserLifespan(ResetCredentialsActionToken.TOKEN_TYPE);
            int absoluteExpirationInSecs = Time.currentTime() + validityInSecs;

            // We send the secret in the email in a link as a query param.
            String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authenticationSession).getEncodedId();
            ResetCredentialsActionToken token = new ResetCredentialsActionToken(
                    user.getId(), absoluteExpirationInSecs, authSessionEncodedId, authenticationSession.getClient().getClientId());

            UriInfo uriInfo = session.getContext().getUri();

            UriBuilder builder = Urls.actionTokenBuilder(uriInfo.getBaseUri(), token.serialize(session, realm, uriInfo),
                    clientModel.getClientId(), authenticationSession.getTabId());
            String link = builder.build(realm.getName()).toString();
            attributes.put("accountLink", link);

            emailSender.blockingSend(new EmailModel(user, realm, subject, template, Collections.emptyList(), attributes,
                    session.theme().getTheme(Theme.Type.EMAIL), session.getContext().resolveLocale(user)));

        } catch (Exception e) {
            log.error("Failed to send email: userId={}, email={}", user.getEmail(), user.getEmail(), e);
        }
    }

    protected UserEntityRepresentation getUserEntityRepresentation(String representation) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(representation, UserEntityRepresentation.class);
    }

    public KeycloakSession getSession() {
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

    public AuthenticationSessionModel createAuthenticationSessionForClient(RealmModel realm, ClientModel client)
            throws UriBuilderException, IllegalArgumentException {
        AuthenticationSessionModel authSession;

        RootAuthenticationSessionModel rootAuthSession = new AuthenticationSessionManager(session).createAuthenticationSession(realm, true);
        authSession = rootAuthSession.createAuthenticationSession(client);

        authSession.setAction(AuthenticationSessionModel.Action.AUTHENTICATE.name());
        authSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        String redirectUri = client.getRedirectUris().stream().findFirst().get();
        authSession.setRedirectUri(redirectUri);
        authSession.setClientNote(OIDCLoginProtocol.REDIRECT_URI_PARAM, redirectUri);
        authSession.setClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM, OAuth2Constants.CODE);
        authSession.setClientNote(OIDCLoginProtocol.ISSUER, Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName()));
        return authSession;
    }
}
