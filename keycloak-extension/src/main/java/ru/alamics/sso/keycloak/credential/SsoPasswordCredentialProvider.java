package ru.alamics.sso.keycloak.credential;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.PasswordCredentialProvider;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCConfigAttributes;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.ws.rs.core.UriBuilder;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SsoPasswordCredentialProvider extends PasswordCredentialProvider {
    private final static String CLIENT_ID = "lkb2b";
    private final static String DEFAULT_CLIENT_ID = "account";
    private final static int VALIDITY_IN_SECS = 259200;

    public SsoPasswordCredentialProvider(KeycloakSession session) {
        super(session);
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
        super.disableCredentialType(realm, user, credentialType);

        if (CredentialModel.PASSWORD.equals(credentialType)) {
            user.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
            sendDisableCredentialEmail(realm, user, VALIDITY_IN_SECS, "emailCredentialDisableSubject", "credential-disable-password.ftl", new HashMap<>());
        }
    }

    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType, int expirationTimeSeconds, String subject, String template, Map<String, Object> attributes) {
        super.disableCredentialType(realm, user, credentialType);

        if (CredentialModel.PASSWORD.equals(credentialType)) {
            user.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
            sendDisableCredentialEmail(realm, user, expirationTimeSeconds, subject, template, attributes);
        }
    }

    private void sendDisableCredentialEmail(RealmModel realm, UserModel user, int expirationTime, String subject, String template, Map<String, Object> attributes) {

        ClientModel clientModel = session.clientStorageManager().getClientByClientId(CLIENT_ID, realm);
        if (clientModel == null)
            clientModel = session.clientStorageManager().getClientByClientId(DEFAULT_CLIENT_ID, realm);
        clientModel.setAttribute(OIDCConfigAttributes.EXCLUDE_SESSION_STATE_FROM_AUTH_RESPONSE, "true");

        AuthenticationSessionManager authenticationSessionManager = new AuthenticationSessionManager(this.session);


        AuthenticationSessionModel authenticationSession = authenticationSessionManager.createAuthenticationSession(realm, false)
                .createAuthenticationSession(clientModel);

        String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authenticationSession).getEncodedId();
        ResetCredentialsActionToken token = new ResetCredentialsActionToken(user.getId(), Time.currentTime() + expirationTime, authSessionEncodedId, clientModel.getClientId());
        UriBuilder builder = Urls.actionTokenBuilder(session.getContext().getUri().getBaseUri(), token.serialize(session, realm, session.getContext().getUri()),
                clientModel.getClientId(), authenticationSession.getTabId());
        String link = builder.build(realm.getName()).toString();

        attributes.put("authHref", link);
        EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);
        try {
            emailTemplateProvider.setRealm(realm)
                    .setUser(user)
                    .send(subject, template, attributes);
        } catch (EmailException e) {
            log.error("error {}", e.getMessage());
        }
    }
}
