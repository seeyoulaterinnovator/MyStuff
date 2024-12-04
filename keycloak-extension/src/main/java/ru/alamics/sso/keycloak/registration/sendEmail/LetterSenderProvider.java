package ru.alamics.sso.keycloak.registration.sendEmail;

import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.actiontoken.verifyemail.VerifyEmailActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.services.Urls;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.schedule.Translator;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.Util;

import java.util.HashMap;
import java.util.Map;

import static ru.alamics.sso.settings.SettingConstants.*;

@Slf4j
public class LetterSenderProvider implements FormAction {

    private static final String BODY_TEMPLATE = "mail-account-create.ftl";

    private SettingsService settingsService;

    public LetterSenderProvider() {
        settingsService = Lookup.lookup(SettingsService.class);
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

        String phone = context.getUser().getFirstAttribute("phone");
        if (phone != null && phone.length() == 11) {
            attributes.put("phone", Util.getFormatNumber(phone));
        }

        attributes.put("emailAccountCreateBodyHtml", settingsService.getSettingsStringValue(EMAIL_CREATE_ACCOUNT, context.getRealm().getName()));

        attributes.put("linkPassword", settingsService.getSettingsStringValue(EMAIL_LINK_PASSWORD, context.getRealm().getName()));

        attributes.put("emailLoginAndPhoneHtml", settingsService.getSettingsStringValue(EMAIL_LOGIN_AND_PHONE_ACCOUNT, context.getRealm().getName()));
        attributes.put("emailLoginHtml", settingsService.getSettingsStringValue(EMAIL_LOGIN_ACCOUNT, context.getRealm().getName()));
        attributes.put("email", context.getUser().getEmail());

        KeycloakSession session = context.getSession();
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        if(session != null && authSession != null && !attributes.containsKey("accountLink")) {
            int timeTokenCreateUser = settingsService.getSettingsIntValue(
                    SettingConstants.TIME_TOKEN_SET_FIRST_PASS,
                    context.getRealm().getName()
            );
            int absoluteExpirationInSecs = Time.currentTime() + timeTokenCreateUser;
            String authSessionEncodedId = SsoUtil.generatePattern();
            VerifyEmailActionToken token = new VerifyEmailActionToken(
                    context.getUser().getId(),
                    absoluteExpirationInSecs,
                    authSessionEncodedId,
                    context.getUser().getEmail(),
                    authSession.getClient().getClientId()
            );
            UriInfo uriInfo = context.getSession().getContext().getUri();
            UriBuilder builder = Urls.actionTokenBuilder(
                    uriInfo.getBaseUri(),
                    token.serialize(context.getSession(), context.getRealm(), uriInfo),
                    authSession.getClient().getClientId(),
                    authSession.getTabId(),
                    context.getHttpRequest().getDecodedFormParameters().getFirst(Constants.CLIENT_DATA)
            );
            String link = builder.build(context.getRealm().getName()).toString();
            attributes.put("accountLink", link);
            int timeTokenResetPass = settingsService.getSettingsIntValue(
                    SettingConstants.TIME_TOKEN_RESET_PASSWORD,
                    context.getRealm().getName()
            );
            attributes.put("expTimePass", Translator.getRusTranslateTimeUnitBySec(timeTokenResetPass));
        }

        try {
            String subject = settingsService.getSettingsStringValue(ACCOUNT_SUBJECT, context.getRealm().getName());
            emailTemplateProvider
                    .setRealm(context.getRealm())
                    .setUser(context.getUser())
                    .send(subject, BODY_TEMPLATE, attributes);
        } catch (EmailException e) {
            log.error("EmailException : {}", e.getMessage(), e);
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
