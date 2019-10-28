package ru.alamics.sso.keycloak.email;

import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.email.freemarker.beans.ProfileBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.theme.FreeMarkerUtil;
import org.keycloak.theme.beans.LinkExpirationFormatterMethod;
import ru.alamics.sso.schedule.Translator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SsoEmailTemplateProvider extends FreeMarkerEmailTemplateProvider implements EmailTemplateProvider {
    public SsoEmailTemplateProvider(KeycloakSession session, FreeMarkerUtil freeMarker) {
        super(session, freeMarker);
    }

    @Override
    public void sendExecuteActions(String link, long expirationInMinutes) throws EmailException {
        Map<String, Object> attributes = new HashMap<String, Object>(this.attributes);
        attributes.put("user", new ProfileBean(user));
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        attributes.put("realmName", getRealmName());
        attributes.put("time", Translator.getRusTranslateTimeUnitBySec(((int) expirationInMinutes) * 60));

        send("executeActionsSubject", "executeActions.ftl", attributes);
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
