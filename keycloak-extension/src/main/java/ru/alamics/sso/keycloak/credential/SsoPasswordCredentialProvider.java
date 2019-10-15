package ru.alamics.sso.keycloak.credential;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
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
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SsoPasswordCredentialProvider extends PasswordCredentialProvider {
    private final static String CLIENT_ID = "lkb2b";
    private final static int VALIDITY_IN_SECS = 259200;

    public SsoPasswordCredentialProvider (KeycloakSession session) {
        super(session);
    }

    @Override
    public void disableCredentialType (RealmModel realm, UserModel user, String credentialType) {
        super.disableCredentialType(realm, user, credentialType);

        if(CredentialModel.PASSWORD.equals(credentialType)) {
            user.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
            sendDisableCredentialEmail(realm, user);
        }
    }

    private void sendDisableCredentialEmail(RealmModel realm, UserModel user){
        int absoluteExpirationInSecs = Time.currentTime() + VALIDITY_IN_SECS;

        ClientModel clientModel = session.clientStorageManager().getClientByClientId(CLIENT_ID, realm);
        clientModel.setAttribute(OIDCConfigAttributes.EXCLUDE_SESSION_STATE_FROM_AUTH_RESPONSE, "true");

        AuthenticationSessionModel authenticationSession = createAuthenticationSessionForClient(realm, clientModel);//rootAuthenticationSessionModel.createAuthenticationSession(clientModel);
        String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authenticationSession).getEncodedId();
        ResetCredentialsActionToken token = new ResetCredentialsActionToken(user.getId(), absoluteExpirationInSecs, authSessionEncodedId, clientModel.getClientId());
        UriBuilder builder = Urls.actionTokenBuilder(session.getContext().getUri().getBaseUri(), token.serialize(session, realm, session.getContext().getUri()),
                clientModel.getClientId(), authenticationSession.getTabId());
        String link = builder.build(realm.getName()).toString();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("authHref", link);
        EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);
        String subject = "emailCredentialDisableSubject";
        String template = "credential-disable-password.ftl";
        try {
            emailTemplateProvider.setRealm(realm)
                    .setUser(user)
                    .send(subject, template, attributes);
        } catch (EmailException e) {
            log.error("error {}", e.getMessage());
        }
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
