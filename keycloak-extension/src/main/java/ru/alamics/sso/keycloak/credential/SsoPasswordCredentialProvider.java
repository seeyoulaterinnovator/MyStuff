package ru.alamics.sso.keycloak.credential;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.PasswordCredentialProvider;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SsoPasswordCredentialProvider extends PasswordCredentialProvider {

    public SsoPasswordCredentialProvider (KeycloakSession session) {
        super(session);
    }

    @Override
    public void disableCredentialType (RealmModel realm, UserModel user, String credentialType) {
        super.disableCredentialType(realm, user, credentialType);
        var emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);

        if(CredentialModel.PASSWORD.equals(credentialType)) {
            String subject = "emailCredentialDisableSubject";
            String template = "credential-disable-password.ftl";
            Map<String, Object> attributes = new HashMap<>();
            URI baseUri = session.getContext().getUri().getBaseUri();
            attributes.put("authHref", baseUri.toString());
            try {
                emailTemplateProvider.setRealm(realm)
                        .setUser(user)
                        .send(subject, template, attributes);
            } catch (EmailException e) {
                log.error("error {}", e.getMessage());
            }
        }
    }
}
