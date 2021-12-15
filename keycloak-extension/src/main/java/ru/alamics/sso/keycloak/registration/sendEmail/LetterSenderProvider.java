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
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.settings.SettingsService;

import java.util.HashMap;
import java.util.Map;

import static ru.alamics.sso.settings.SettingConstants.ACCOUNT_SUBJECT;
import static ru.alamics.sso.settings.SettingConstants.EMAIL_CREATE_ACCOUNT;

@Slf4j
public class LetterSenderProvider implements FormAction {

    private static final String BODY_TEMPLATE = "mail-account-create.ftl";

    private SettingsService settingsService;

    public LetterSenderProvider() {
        settingsService = (SettingsService) Lookup.lookup(SettingsService.class);
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
        attributes.put("emailAccountCreateBodyHtml", settingsService.getSettingsStringValue(EMAIL_CREATE_ACCOUNT,context.getRealm().getName()));
        try {
            String subject = settingsService.getSettingsStringValue(ACCOUNT_SUBJECT, context.getRealm().getName());
            emailTemplateProvider
                    .setRealm(context.getRealm())
                    .setUser(context.getUser())
                    .send(subject, BODY_TEMPLATE, attributes);
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
