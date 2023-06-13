package ru.alamics.sso.keycloak.auth.requiredactions;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.requiredactions.UpdatePassword;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.client.ClientService;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.Util;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class SsoUpdatePassword extends UpdatePassword {
    private final static String DEFAULT_CLIENT_ID = "account";
    private static final String GRANT_TYPE = "grant_type";
    private static final String UPDATE_PASSWORD_FTL = "login-update-password.ftl";
    private static final String PASSWORD = "password";
    private static final String MOBILE_APP = "MP";
    private SettingsService settingsService;

    @Override
    public void processAction(RequiredActionContext context) {
        EventBuilder event = context.getEvent();
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        event.event(EventType.UPDATE_PASSWORD);
        String passwordNew = formData.getFirst("password-new");

        EventBuilder errorEvent = event.clone().event(EventType.UPDATE_PASSWORD_ERROR)
                .client(context.getAuthenticationSession().getClient())
                .user(context.getAuthenticationSession().getAuthenticatedUser());

        String mp = context.getAuthenticationSession().getAuthNote("MP");
        if (mp != null) {
            if (!isValidPassword(passwordNew)) {
                Response response = context.form().createResponse(UserModel.RequiredAction.UPDATE_PASSWORD);
                Map<String, String> entity = (Map<String, String>) response.getEntity();
                entity.put("error", "Пароль не прошел валидацию, попробуйте еще раз");
                context.challenge(response);
                return;
            }
        }

        if (Validation.isBlank(passwordNew)) {
            Response challenge = context.form()
                    .setAttribute("username", context.getAuthenticationSession().getAuthenticatedUser().getUsername())
                    .setError(Messages.MISSING_PASSWORD)
                    .createResponse(UserModel.RequiredAction.UPDATE_PASSWORD);
            context.challenge(challenge);
            errorEvent.error(Errors.PASSWORD_MISSING);
            return;
        }

        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();

        try {
            context.getSession().userCredentialManager().updateCredential(context.getRealm(), context.getUser(), UserCredentialModel.password(passwordNew, false));

            context.success();
        } catch (ModelException me) {
            errorEvent.detail(Details.REASON, me.getMessage()).error(Errors.PASSWORD_REJECTED);
            Response challenge = context.form()
                    .setAttribute("username", context.getAuthenticationSession().getAuthenticatedUser().getUsername())
                    .setError(me.getMessage(), me.getParameters())
                    .createResponse(UserModel.RequiredAction.UPDATE_PASSWORD);
            context.challenge(challenge);
            return;
        } catch (Exception ape) {
            errorEvent.detail(Details.REASON, ape.getMessage()).error(Errors.PASSWORD_REJECTED);
            Response challenge = context.form()
                    .setAttribute("username", context.getAuthenticationSession().getAuthenticatedUser().getUsername())
                    .setError(ape.getMessage())
                    .createResponse(UserModel.RequiredAction.UPDATE_PASSWORD);
            context.challenge(challenge);
            return;
        }

        setRedirectAfterAction(context);

    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {

        context.challenge(createForm(context, context.form()));

    }

    private Response createForm(RequiredActionContext context, LoginFormsProvider loginFormsProvider) {
        //Костыль тк при запросе с МП не нашел другого способа верификацию отправить по rest
        String mp = context.getAuthenticationSession().getAuthNote(MOBILE_APP);

        if (mp != null) {
            HttpRequest contextObject = context.getSession().getContext().getContextObject(HttpRequest.class);
            MultivaluedMap<String, String> parameters = contextObject.getDecodedFormParameters();
            parameters.add(GRANT_TYPE, PASSWORD);
        }
        return loginFormsProvider.createForm(UPDATE_PASSWORD_FTL);
    }

    private void setRedirectAfterAction(RequiredActionContext context) {
        final KeycloakSession session = context.getSession();
        final AuthenticationSessionModel currentAuthenticationSession = context.getAuthenticationSession();

        if (settingsService == null) {
            settingsService = Lookup.lookup(SettingsService.class);
        }

        ClientModel client = session.getContext().getClient();

        if (client == null) {
            String defaultClientRealm = settingsService.getSettingsStringValue(SettingConstants.DEFAULT_REALM_CLIENT_ID, context.getRealm().getId());
            client = session.clientStorageManager().getClientByClientId(defaultClientRealm, currentAuthenticationSession.getRealm());
            if (client == null) {
                client = session.clientStorageManager().getClientByClientId(DEFAULT_CLIENT_ID, currentAuthenticationSession.getRealm());
                if (client == null) {
                    log.error("Redirect after UPDATE_PASSWORD is not setup: clientId={} not found", defaultClientRealm);
                    return;
                }
            }
        }


        //т.к. при старте новой сессии задается этот параметр = true, редирект после прохожения всего флоу не происходит
        currentAuthenticationSession.setAuthNote(AuthenticationManager.END_AFTER_REQUIRED_ACTIONS, null);

//        ломает следующий за восстановлением пароля required action
//        currentAuthenticationSession.setAction(AuthenticationSessionModel.Action.AUTHENTICATE.name());
        currentAuthenticationSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);


        final ClientService clientService = Lookup.lookup(ClientService.class);

        if (clientService == null) {
            log.error("ClientService failed lookup. Redirect by clientId={} is not possible", client.getClientId());
            return;
        }

        String redirectUri = clientService.findMainRedirectUri(client);

        currentAuthenticationSession.setRedirectUri(redirectUri);
        currentAuthenticationSession.setClientNote(OIDCLoginProtocol.REDIRECT_URI_PARAM, redirectUri);
        currentAuthenticationSession.setClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM, OAuth2Constants.CODE);
        currentAuthenticationSession.setClientNote(OIDCLoginProtocol.ISSUER, Urls.realmIssuer(session.getContext().getUri().getBaseUri(), currentAuthenticationSession.getRealm().getName()));
    }

    private boolean isValidPassword(String password) {
        return Pattern.matches(Util.REGEX_PASSWORD, password);
    }
}
