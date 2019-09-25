package ru.alamics.sso.keycloak.requiredactions;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.PasswordCredentialProvider;
import org.keycloak.credential.PasswordCredentialProviderFactory;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.UserModel;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class UpdatePassword extends org.keycloak.authentication.requiredactions.UpdatePassword {

    @Override
    public void evaluateTriggers (RequiredActionContext context) {
        int daysToExpirePassword = context.getRealm().getPasswordPolicy().getDaysToExpirePassword();
        if(daysToExpirePassword != -1) {
            PasswordCredentialProvider passwordProvider = (PasswordCredentialProvider)context.getSession().getProvider(CredentialProvider.class, PasswordCredentialProviderFactory.PROVIDER_ID);
            CredentialModel password = passwordProvider.getPassword(context.getRealm(), context.getUser());
            if (password != null) {
                if(password.getCreatedDate() == null) {
                    context.getUser().addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
                    log.debug("User is required to update password");
                } else {
                    long timeElapsed = Time.toMillis(Time.currentTime()) - password.getCreatedDate();
                    long timeToExpire = TimeUnit.DAYS.toMillis(daysToExpirePassword);

                    if(timeElapsed > timeToExpire) {
                        context.getUser().addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
                        sendEmail(context);
                        log.debug("User is required to update password");
                    }
                }
            }
        }
    }

    private void sendEmail(RequiredActionContext context) {
        var session = context.getSession();
        var realm = context.getRealm();
        var subject = "emailExpiresPasswordDataSubject";
        var template = "password-expires.ftl";
        EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);
        var user = context.getUser();

        try {
            emailTemplateProvider
                    .setRealm(realm)
                    .setUser(user)
                    .send(subject, template, new HashMap<>());

        } catch (EmailException e) {
            log.error("{}", e.getMessage());
        }
    }

}
