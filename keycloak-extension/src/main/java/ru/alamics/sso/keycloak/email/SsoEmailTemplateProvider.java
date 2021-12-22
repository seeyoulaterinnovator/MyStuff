package ru.alamics.sso.keycloak.email;

import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.email.freemarker.beans.ProfileBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.FreeMarkerUtil;
import org.keycloak.theme.Theme;
import org.keycloak.theme.beans.LinkExpirationFormatterMethod;
import org.keycloak.theme.beans.MessageFormatterMethod;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.schedule.Translator;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import static ru.alamics.sso.settings.SettingConstants.*;
import static ru.alamics.sso.settings.SettingConstants.GRATITUDE_DOWN;

public class SsoEmailTemplateProvider extends FreeMarkerEmailTemplateProvider implements EmailTemplateProvider {

    private static final String BODY_TEMPLATE_PASS_RESET = "password-reset.ftl";
    private static final String BODY_TEMPLATE_EXECUTE_ACTIONS = "executeActions.ftl";
    private static final String BODY_TEMPLATE_IDENTITY_PROVIDER_LINK = "identity-provider-link.ftl";
    private static final String BODY_TEMPLATE_EMAIL_VERIFICATION = "email-verification.ftl";

    private SettingsService settingsService;

    public SsoEmailTemplateProvider(KeycloakSession session, FreeMarkerUtil freeMarker) {
        super(session, freeMarker);
        settingsService = (SettingsService) Lookup.lookup(SettingsService.class);
    }

    @Override
    public void sendExecuteActions(String link, long expirationInMinutes) throws EmailException {
        Map<String, Object> attributes = new HashMap<String, Object>(this.attributes);
        attributes.put("user", new ProfileBean(user));
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        attributes.put("realmName", getRealmName());
        attributes.put("time", Translator.getRusTranslateTimeUnitBySec(((int) expirationInMinutes) * 60));
        attributes.put("executeActionsBodyHtml", settingsService.getSettingsStringValue(EMAIL_ACTIONS_ACCOUNT,realm.getName()));

        send(settingsService.getSettingsStringValue(ACCOUNT_SUBJECT_ACTIONS,realm.getName()), BODY_TEMPLATE_EXECUTE_ACTIONS, attributes);
    }

    @Override
    public void sendVerifyEmail(String link, long expirationInMinutes) throws EmailException {
        Map<String, Object> attributes = new HashMap<String, Object>(this.attributes);
        attributes.put("user", new ProfileBean(user));
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        attributes.put("realmName", getRealmName());
        attributes.put("emailVerificationBodyHtml", settingsService.getSettingsStringValue(SettingConstants.EMAIL_VERIFICATION_ACCOUNT,realm.getName()));

        send(settingsService.getSettingsStringValue(ACCOUNT_SUBJECT_VERIFICATION,realm.getName()), BODY_TEMPLATE_EMAIL_VERIFICATION, attributes);
    }

    @Override
    public void sendConfirmIdentityBrokerLink(String link, long expirationInMinutes) throws EmailException {
        Map<String, Object> attributes = new HashMap<String, Object>(this.attributes);
        attributes.put("user", new ProfileBean(user));
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        attributes.put("realmName", getRealmName());

        BrokeredIdentityContext brokerContext = (BrokeredIdentityContext) this.attributes.get(IDENTITY_PROVIDER_BROKER_CONTEXT);
        String idpAlias = brokerContext.getIdpConfig().getAlias();
        idpAlias = ObjectUtil.capitalize(idpAlias);

        attributes.put("identityProviderContext", brokerContext);
        attributes.put("identityProviderAlias", idpAlias);
        attributes.put("identityProviderLinkBodyHtml", settingsService.getSettingsStringValue(EMAIL_IDENTITY_PROVIDER,realm.getName()));

        List<Object> subjectAttrs = Arrays.<Object> asList(idpAlias);
        send(settingsService.getSettingsStringValue(ACCOUNT_SUBJECT_PROVIDER_LINK,realm.getName()), subjectAttrs, BODY_TEMPLATE_IDENTITY_PROVIDER_LINK, attributes);
    }

    @Override
    public void sendPasswordReset(String link, long expirationInMinutes) throws EmailException {
        Map<String, Object> attributes = new HashMap<String, Object>(this.attributes);
        attributes.put("user", new ProfileBean(user));
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        attributes.put("realmName", getRealmName());

        attributes.put("passwordResetBodyHtml", settingsService.getSettingsStringValue(EMAIL_RESET,realm.getName()));


        send(settingsService.getSettingsStringValue(ACCOUNT_SUBJECT_RESET,realm.getName()), BODY_TEMPLATE_PASS_RESET, attributes);
    }

    @Override
    protected EmailTemplate processTemplate(String subjectKey, List<Object> subjectAttributes, String template, Map<String, Object> attributes) throws EmailException {
        try {
            Theme theme = getTheme();
            Locale locale = session.getContext().resolveLocale(user);
            attributes.put("locale", locale);
            Properties rb = theme.getMessages(locale);
            attributes.put("msg", new MessageFormatterMethod(locale, rb));
            attributes.put("properties", theme.getProperties());
            attributes.put("phoneInMessage", settingsService.getSettingsStringValue(PHONE_IN_MESSAGE,realm.getName()));
            attributes.put("footerInMassage", settingsService.getSettingsStringValue(FOOTER_IN_MESSAGE,realm.getName()));
            attributes.put("customer", settingsService.getSettingsStringValue(CUSTOMER,realm.getName()));
            attributes.put("gratitudeUp", settingsService.getSettingsStringValue(GRATITUDE_UP,realm.getName()));
            attributes.put("gratitudeDown", settingsService.getSettingsStringValue(GRATITUDE_DOWN,realm.getName()));
            attributes.put("phoneConstLink", settingsService.getSettingsStringValue(PHONE_CONST_LINK,realm.getName()));
            attributes.put("homePage", settingsService.getSettingsStringValue(HOME_PAGE,realm.getName()));
            String subject = new MessageFormat(rb.getProperty(subjectKey, subjectKey), locale).format(subjectAttributes.toArray());
            String textTemplate = String.format("text/%s", template);
            String textBody;
            try {
                textBody = freeMarker.processTemplate(attributes, textTemplate, theme);
            } catch (final FreeMarkerException e) {
                textBody = null;
            }
            String htmlTemplate = String.format("html/%s", template);
            String htmlBody;
            try {
                htmlBody = freeMarker.processTemplate(attributes, htmlTemplate, theme);
            } catch (final FreeMarkerException e) {
                htmlBody = null;
            }

            return new EmailTemplate(subject, textBody, htmlBody);
        } catch (Exception e) {
            throw new EmailException("Failed to template email", e);
        }
    }

    @Override
    protected void addLinkInfoIntoAttributes(String link, long expirationInMinutes, Map<String, Object> attributes) throws EmailException {
        attributes.put("link", link);
        attributes.put("linkExpiration", expirationInMinutes);
        try {
            Locale locale = session.getContext().resolveLocale(user);
            attributes.put("linkExpirationFormatter", new LinkExpirationFormatterMethod(getTheme().getMessages(locale), locale));
        } catch (IOException e) {
            throw new EmailException("Failed to template email", e);
        }
    }
}
