package ru.alamics.sso.keycloak.credential;

import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.credential.PasswordCredentialProvider;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.protocol.oidc.OIDCConfigAttributes;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.keycloak.util.PhoneFormatter;
import ru.alamics.sso.schedule.Translator;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;

import java.util.HashMap;
import java.util.Map;

import static ru.alamics.sso.settings.SettingConstants.*;

@Slf4j
public class SsoPasswordCredentialProvider extends PasswordCredentialProvider {
    private final static String DEFAULT_CLIENT_ID = "account";
    private final static int VALIDITY_IN_SECS = 259200;

    private SettingsService settingsService;

    public SsoPasswordCredentialProvider(KeycloakSession session) {
        super(session);
        settingsService = Lookup.lookup(SettingsService.class);
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
        super.disableCredentialType(realm, user, credentialType);

        if (PasswordCredentialModel.TYPE.equals(credentialType)) {
            user.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
            sendDisableCredentialEmail(realm, user, VALIDITY_IN_SECS, settingsService.getSettingsStringValue(ACCOUNT_SUBJECT_CREDENTIAL_DISABLE, realm.getName()), "credential-disable-password.ftl", new HashMap<>());
        }
    }

    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType, int expirationTimeSeconds, String subject, String template, Map<String, Object> attributes) {
        super.disableCredentialType(realm, user, credentialType);

        if (PasswordCredentialModel.TYPE.equals(credentialType)) {
            user.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
            sendDisableCredentialEmail(realm, user, expirationTimeSeconds, subject, template, attributes);
        }
    }
    //here disable cred email
    private void sendDisableCredentialEmail(RealmModel realm, UserModel user, int expirationTime, String subject, String template, Map<String, Object> attributes) {


        String clientId = settingsService.getSettingsStringValue(DEFAULT_REALM_CLIENT_ID, realm.getName());
        ClientModel clientModel = session.getProvider(ClientProvider.class).getClientByClientId(realm, clientId);
        if (clientModel == null)
            clientModel = session.getProvider(ClientProvider.class).getClientByClientId(realm, DEFAULT_CLIENT_ID);
        clientModel.setAttribute(OIDCConfigAttributes.EXCLUDE_SESSION_STATE_FROM_AUTH_RESPONSE, "true");

        AuthenticationSessionManager authenticationSessionManager = new AuthenticationSessionManager(this.session);


        AuthenticationSessionModel authenticationSession = authenticationSessionManager.createAuthenticationSession(realm, false)
                .createAuthenticationSession(clientModel);
        authenticationSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);

        String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authenticationSession).getEncodedId();
        ResetCredentialsActionToken token = new ResetCredentialsActionToken(
                user.getId(),
                user.getEmail(),
                Time.currentTime() + expirationTime,
                authSessionEncodedId,
                clientModel.getClientId()
        );
        UriBuilder builder = Urls.actionTokenBuilder(
                session.getContext().getUri().getBaseUri(),
                token.serialize(session, realm, session.getContext().getUri()),
                clientModel.getClientId(),
                authenticationSession.getTabId(),
                AuthenticationProcessor.getClientData(session, authenticationSession)
        );

        int timeTokenResetPass = settingsService.getSettingsIntValue(SettingConstants.TIME_TOKEN_RESET_PASSWORD, realm.getName());
        String expirationStrRus = Translator.getRusTranslateTimeUnitBySec(timeTokenResetPass);
        String link = builder.build(realm.getName()).toString();
        String email = user.getEmail();

        attributes.put("authHref", link);
        attributes.put("emailCredentialDisableBodyHtmlCost", settingsService.getSettingsStringValue(EMAIL_CREDENTIAL_DISABLE_ACCOUNT, realm.getName()));
        attributes.put("expTime", expirationStrRus);
        attributes.put("email", email);

        if(UserModelUserMapper.mapToUser(user).getPhone() != null) {
            attributes.put("phone", PhoneFormatter.formatPhoneNumber(UserModelUserMapper.mapToUser(user).getPhone().trim()));
        }
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
