package ru.alamics.sso.keycloak.registration.sendEmail;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LetterSenderProvider implements FormAction {

    public LetterSenderProvider() {
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {

    }

    @Override
    public void validate(ValidationContext context) {
        context.success();
    }

    @Override
    public void success(FormContext context) {
        log.info("start sendEmailRegistration");
        EmailTemplateProvider emailTemplateProvider = context.getSession().getProvider(EmailTemplateProvider.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userName", context.getUser().getUsername());
        try {
            emailTemplateProvider
                    .setRealm(context.getRealm())
                    .setUser(context.getUser())
                    .send("emailAccountDataSubject", "mail-account-create.ftl", attributes);
        } catch (EmailException e){
            log.error("EmailException : {}",e);
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }
}
