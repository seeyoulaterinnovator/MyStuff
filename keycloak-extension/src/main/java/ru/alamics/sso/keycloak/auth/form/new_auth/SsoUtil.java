package ru.alamics.sso.keycloak.auth.form.new_auth;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.common.util.Time;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.alamics.sso.auth_n_regi.AuthOrRegTypeNotFoundException;
import ru.alamics.sso.jpa.entity.auth_reg.AuthOrRegType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.utils.OAuth2Code;
import org.keycloak.protocol.oidc.utils.OAuth2CodeParser;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.MediaType;
import ru.alamics.sso.keycloak.auth.form.new_auth.common_mail_sender.EmailSenderService;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ru.alamics.sso.registration.model.UserConstants.REDIRECT_URI;

public interface SsoUtil {

    Logger log = LoggerFactory.getLogger(SsoUtil.class);


    Map<String, String> responseBody = new HashMap<>();

    static void sendEmailVer(RequiredActionContext context) {
        log.info("void sendEmailVer is called");
        if (!context.getUser().isEmailVerified()) {
            log.info("if ver : " + !context.getUser().isEmailVerified());
            EmailSenderService.sendVerifyEmail(context.getSession(),
                    context.form(),
                    context.getUser(),
                    context.getAuthenticationSession(),
                    context.getEvent().clone().event(EventType.SEND_VERIFY_EMAIL).detail(Details.EMAIL, context.getUser().getEmail()));
        }
        log.info("else");
    }

    static String generatePattern() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        String randomString = generateRandomString(11);
        return uuid1 + "." + randomString + "." + uuid2;
    }

    static String generateRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    static boolean sendEmailVer(UserModel userModel, LoginFormsProvider lfp, KeycloakSession session, AuthenticationSessionModel sessionModel, EventBuilder eventBuilder) {
        log.info("boolean sendEmailVer: is called ");
        if (userModel != null && !userModel.isEmailVerified()) {
            log.info("boolean sendEmailVer: " + userModel.getUsername());
            EmailSenderService.sendVerifyEmail(session,
                    lfp,
                    userModel,
                    sessionModel,
                    eventBuilder.clone().event(EventType.SEND_VERIFY_EMAIL).detail(Details.EMAIL, userModel.getEmail()));
            return false;
        }
        log.info("boolean return true");
        return true;
    }


    static boolean addRequiredAction(AuthenticationFlowContext context, String providerName, UserModel userModel) {
        RequiredActionProviderModel providerModel = context.getRealm().getRequiredActionProviderByAlias(providerName);

        if (providerModel.isEnabled() && !userModel.getRequiredActions().contains(providerName)) {
            userModel.addRequiredAction(providerName);
            return true;
        }
        return false;
    }

    static void decideResponseFormat(AuthenticationFlowContext context, AuthenticationSessionModel authSession) {
        UriInfo uriInfo = context.getUriInfo();
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        Map<String, String> redirectUriQueryParams = extractQueryParamsFromRedirectUri(queryParams.getFirst(REDIRECT_URI));
        if (authSession.getAuthNote("format") == null) {
            authSession.setAuthNote("format", redirectUriQueryParams.get("format"));
        }
    }

    static Map<String, String> extractQueryParamsFromRedirectUri(String redirectUri) {
        Map<String, String> queryParameters = new HashMap<>();
        if (redirectUri != null && redirectUri.indexOf('?') >= 0) {
            redirectUri = redirectUri.substring(redirectUri.indexOf('?') + 1);
            String[] pairs = redirectUri.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx >= 0) {
                    try {
                        queryParameters.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name()), URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name()));
                    } catch (UnsupportedEncodingException ignore) {

                    }
                }
            }
        }
        return queryParameters;
    }

    static UserSessionModel isUserAuthenticated(AuthenticationFlowContext context, AuthenticationSessionModel sessionModel) {
        UserSessionProvider userSessionProvider = context.getSession().sessions();
        List<UserSessionModel> activeSessions = userSessionProvider.getUserSessions(context.getSession().getContext().getRealm(), context.getUser());
        if (!activeSessions.isEmpty()) {
            sessionModel.setAuthNote("authenticated", "");
            return activeSessions.get(0);
        }
        return null;
    }

    static boolean decideResponse(AuthenticationFlowContext context, AuthenticationSessionModel sessionModel, UserSessionModel userSessionModel, KeycloakSession session) {
        String authenticated = sessionModel.getAuthNote("authenticated");
        String format = sessionModel.getAuthNote("format");
        if (format != null) {
            Map<String, String> entity = new HashMap<>();
            if (authenticated != null) {
                fillResponseBody(context.getAuthenticationSession(), userSessionModel, TokenManager.attachAuthenticationSession(session, userSessionModel, sessionModel), session);
                entity.put("code", responseBody.get("code"));
                entity.put("state", responseBody.get("state"));
                entity.put("session_state", responseBody.get("session_state"));
                context.forceChallenge(Response.ok().entity(entity).type(MediaType.APPLICATION_JSON_TYPE).build());
            } else {
                entity.put("error", "format param while user has no active sessions");
                context.forceChallenge(Response.status(400).entity(entity).type(MediaType.APPLICATION_JSON_TYPE).build());
            }
            return false;
        }
        return true;
    }

    static void decideResponse(AuthenticationFlowContext context) {
        Map<String, String> entity = new HashMap<>();
        entity.put("error", "format param while user has no active sessions");
        context.forceChallenge(Response.status(400).entity(entity).type(MediaType.APPLICATION_JSON_TYPE).build());
    }

    static void fillResponseBody(AuthenticationSessionModel authSession, UserSessionModel userSession, ClientSessionContext clientSessionCtx, KeycloakSession session) {
        AuthenticatedClientSessionModel clientSession = clientSessionCtx.getClientSession();

        String state = authSession.getClientNote(OIDCLoginProtocol.STATE_PARAM);
        responseBody.put("state", state);

        OIDCAdvancedConfigWrapper clientConfig = OIDCAdvancedConfigWrapper.fromClientModel(clientSession.getClient());
        if (!clientConfig.isExcludeSessionStateFromAuthResponse()) {
            responseBody.put("session_state", userSession.getId());
        }

        String nonce = authSession.getClientNote(OIDCLoginProtocol.NONCE_PARAM);
        clientSessionCtx.setAttribute(OIDCLoginProtocol.NONCE_PARAM, nonce);

        OAuth2Code codeData = new OAuth2Code(UUID.randomUUID(),
                Time.currentTime() + userSession.getRealm().getAccessCodeLifespan(),
                nonce,
                authSession.getClientNote(OAuth2Constants.SCOPE),
                authSession.getClientNote(OIDCLoginProtocol.REDIRECT_URI_PARAM),
                authSession.getClientNote(OIDCLoginProtocol.CODE_CHALLENGE_PARAM),
                authSession.getClientNote(OIDCLoginProtocol.CODE_CHALLENGE_METHOD_PARAM));

        String code = OAuth2CodeParser.persistCode(session, clientSession, codeData);
        responseBody.put("code", code);
    }

    static int getAuthOrRegType(AuthenticationSessionModel authenticationSessionModel) throws AuthOrRegTypeNotFoundException{
        AuthOrRegType[] authOrRegTypes = AuthOrRegType.values();

        for (AuthOrRegType authOrRegType : authOrRegTypes) {
            if (authenticationSessionModel.getAuthNote(authOrRegType.getButtonName()) != null) {
                return authOrRegType.getId();
            }
        }
        throw new AuthOrRegTypeNotFoundException("Auth or Reg Type Not Found");
    }
}
