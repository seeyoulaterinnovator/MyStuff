package ru.alamics.sso.keycloak.event.listener.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionToken;
import org.keycloak.authentication.actiontoken.verifyemail.VerifyEmailActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.events.Details;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.theme.Theme;
import ru.alamics.sso.emailer.EmailModel;
import ru.alamics.sso.emailer.EmailSender;
import ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil;
import ru.alamics.sso.keycloak.auth.form.new_auth.common_mail_sender.EmailSenderService;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.model.UserEntityRepresentation;
import ru.alamics.sso.schedule.Translator;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class SsoEvent {
    private final static String DEFAULT_CLIENT_ID = "account";

    private final KeycloakSession session;
    private final EmailSender emailSender;
    private final SettingsService settingsService;

    public SsoEvent(KeycloakSession session) {
        this.session = session;
        this.emailSender = Lookup.lookup(EmailSender.class);
        settingsService = Lookup.lookup(SettingsService.class);
    }

    public abstract void execute();

    protected void sendEmail(UserModel user, RealmModel realm, String subject, String template, Map<String, Object> attributes) {
        try {
            String defaultClientRealm = settingsService.getSettingsStringValue(SettingConstants.DEFAULT_REALM_CLIENT_ID, realm.getId());
            ClientModel clientModel = session.clientStorageManager().getClientByClientId(defaultClientRealm, realm);
            if (clientModel == null)
                clientModel = session.clientStorageManager().getClientByClientId(DEFAULT_CLIENT_ID, realm);
            if (clientModel == null) {
                log.error("Failed to send email: {}", "have no client=\"" + defaultClientRealm + "\" to redirect!");
                return;
            }

            AuthenticationSessionManager authenticationSessionManager = new AuthenticationSessionManager(this.session);

            AuthenticationSessionModel authenticationSession = authenticationSessionManager.createAuthenticationSession(realm, false)
                    .createAuthenticationSession(clientModel);

            int timeTokenCreateUser = settingsService.getSettingsIntValue(SettingConstants.TIME_TOKEN_SET_FIRST_PASS, realm.getName());
            int absoluteExpirationInSecs = Time.currentTime() + timeTokenCreateUser;

              boolean isContainsPhone = user.getAttribute("phone").size() != 0;
            String keyPrefix = "migration";
            boolean isContainsMigration = user.getAttributes().keySet().stream()
                    .anyMatch(key -> key.startsWith(keyPrefix));
            log.info("isContainsMigration is " + isContainsMigration);
            if (isContainsPhone && !isContainsMigration) {
                log.info("ContainsPhone && ContainsMigration");
//                UriInfo uriInfo = session.getContext().getUri();
                String authSessionEncodedId = SsoUtil.generatePattern();
                VerifyEmailActionToken token = new VerifyEmailActionToken(user.getId(), absoluteExpirationInSecs, authSessionEncodedId, user.getEmail(), authenticationSession.getClient().getClientId());
//                UriBuilder builder = Urls.actionTokenBuilder(uriInfo.getBaseUri(), token.serialize(session, realm, uriInfo),
//                        authenticationSession.getClient().getClientId(), authenticationSession.getTabId());
//                String link = builder.build(realm.getName()).toString();
//                long expirationInMinutes = TimeUnit.SECONDS.toMinutes(timeTokenVerifyEmail);
//
//                String expirationStrRus = Translator.getRusTranslateTimeUnitBySec(timeTokenVerifyEmail);

//                String link = builder.build(realm.getName()).toString();
//                long expirationInMinutes = TimeUnit.SECONDS.toMinutes(timeTokenVerifyEmail);


                UriInfo uriInfo = session.getContext().getUri();

                UriBuilder builder = Urls.actionTokenBuilder(uriInfo.getBaseUri(), token.serialize(session, realm, uriInfo),
                        clientModel.getClientId(), authenticationSession.getTabId());


                String link = builder.build(realm.getName()).toString();
                attributes.put("accountLink", link);

                String expirationStrRusPass = Translator.getRusTranslateTimeUnitBySec(timeTokenCreateUser);
                attributes.put("expTimePass", expirationStrRusPass);
                emailSender.send(new EmailModel(user, realm, subject, template, Collections.emptyList(), attributes,
                        session.theme().getTheme(Theme.Type.EMAIL), session.getContext().resolveLocale(user)));


            } else {
                log.info("NO ContainsPhone");
//                при создании пользователя через админку, почта подтверждается автоматом

                user.setEmailVerified(true);

                // We send the secret in the email in a link as a query param.
                String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authenticationSession).getEncodedId();

                ResetCredentialsActionToken token = new ResetCredentialsActionToken(
                        user.getId(), absoluteExpirationInSecs, authSessionEncodedId, authenticationSession.getClient().getClientId());

                UriInfo uriInfo = session.getContext().getUri();

                UriBuilder builder = Urls.actionTokenBuilder(uriInfo.getBaseUri(), token.serialize(session, realm, uriInfo),
                        clientModel.getClientId(), authenticationSession.getTabId());


                String link = builder.build(realm.getName()).toString();
                attributes.put("accountLink", link);

                String expirationStrRusPass = Translator.getRusTranslateTimeUnitBySec(timeTokenCreateUser);
                attributes.put("expTimePass", expirationStrRusPass);


                emailSender.send(new EmailModel(user, realm, subject, template, Collections.emptyList(), attributes,
                        session.theme().getTheme(Theme.Type.EMAIL), session.getContext().resolveLocale(user)));

            }


        } catch (Exception e) {
            log.error("Failed to send email: userId={}, email={}", user.getEmail(), user.getEmail(), e);
        }
    }

    protected UserEntityRepresentation getUserEntityRepresentation(String representation) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(representation, UserEntityRepresentation.class);
    }

    public KeycloakSession getSession() {
        return session;
    }

    public String getUserId(AdminEvent event) {
        String[] resPath = event.getResourcePath().split("/");

        String userId = null;
        for (String part : resPath) {
            if (part.length() == 36) {
                userId = part;
                break;
            }
        }
        return userId;
    }
}
