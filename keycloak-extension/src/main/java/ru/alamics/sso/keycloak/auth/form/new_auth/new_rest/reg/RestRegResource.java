package ru.alamics.sso.keycloak.auth.form.new_auth.new_rest.reg;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilderException;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.models.utils.SystemClientUtil;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.resources.SessionCodeChecks;
import org.keycloak.services.util.BrowserHistoryHelper;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import ru.alamics.sso.keycloak.mobile.util.CustomAuthenticationFlowResolver;

import java.util.Map;

public class RestRegResource {

    public static final String REGISTRATION_PATH = "registration";

    public static final String FORWARDED_ERROR_MESSAGE_NOTE = "forwardedErrorMessage";

    public static final String SESSION_CODE = "session_code";
    public static final String AUTH_SESSION_ID = "auth_session_id";
    private final KeycloakSession session;
    private final RealmModel realm;
    private final ClientConnection clientConnection;

    private final EventBuilder event;
    private final HttpRequest request;

    RestRegResource(KeycloakSession session) {
        this.session = session;

        this.realm = session.getContext().getRealm();

        this.clientConnection = session.getContext().getConnection();

        this.event = new EventBuilder(realm, session, clientConnection);

        this.request = session.getContext().getHttpRequest();
    }

    @Path(REGISTRATION_PATH)
    @POST
    public Response registerPage(@QueryParam(AUTH_SESSION_ID) String authSessionId, // optional, can get from cookie instead
                                 @QueryParam(SESSION_CODE) String code,
                                 @QueryParam(Constants.EXECUTION) String execution,
                                 @QueryParam(Constants.CLIENT_ID) String clientId,
                                 @QueryParam(Constants.TAB_ID) String tabId) {

        Response response = createFlow(clientId, tabId, code);

        if (Response.Status.Family.CLIENT_ERROR.equals(response.getStatusInfo().getFamily())) {
            return response;
        }

        Map<String, String> entity = (Map<String, String>) response.getEntity();
        String ex = entity.get("execution");
        String accessCode = entity.get("access_code");
        String sessionState = entity.get("session_state");
        String tab = entity.get("tab_id");

        return registerRequest(sessionState, accessCode, ex, clientId, tab, true);

    }

    private SessionCodeChecks checksForCode(String authSessionId, String code, String execution, String clientId, String tabId, String flowPath) {
        SessionCodeChecks res = new SessionCodeChecks(realm, session.getContext().getUri(), request, clientConnection,
                session, event, authSessionId, code, execution, clientId, tabId,
                request.getDecodedFormParameters().getFirst(Constants.CLIENT_DATA), flowPath);
        res.initialVerify();
        return res;
    }

    private Response registerRequest(String authSessionId, String code, String execution, String clientId, String tabId, boolean isPostRequest) {
        event.event(EventType.REGISTER);
        if (!realm.isRegistrationAllowed()) {
            event.error(Errors.REGISTRATION_DISABLED);
            return ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.REGISTRATION_NOT_ALLOWED);
        }

        SessionCodeChecks checks = checksForCode(authSessionId, code, execution, clientId, tabId, REGISTRATION_PATH);
        if (!checks.verifyActiveAndValidAction(AuthenticationSessionModel.Action.AUTHENTICATE.name(), ClientSessionCode.ActionType.LOGIN)) {
            return checks.getResponse();
        }

        AuthenticationSessionModel authSession = checks.getAuthenticationSession();

        AuthenticationManager.expireIdentityCookie(session);

        return processRegistration(checks.isActionRequest(), execution, authSession, null);
    }

    private Response createFlow(String clientId, String tabId, String code) {
        ClientModel client = realm.getClientByClientId(clientId);
        AuthenticationSessionModel authSession = new AuthenticationSessionManager(session).getCurrentAuthenticationSession(realm, client, tabId);
        if (authSession == null && code == null) {
            if (!realm.isResetPasswordAllowed()) {
                event.event(EventType.REGISTER);
                event.error(Errors.NOT_ALLOWED);
                return ErrorResponse.error(Messages.REGISTRATION_NOT_ALLOWED, Response.Status.BAD_REQUEST).getResponse();
            }
            return processRegistration(false, null, createAuthenticationSessionForClient(clientId), null);
        }
        return ErrorResponse.error("Не удалось создать flow", Response.Status.NOT_FOUND).getResponse();
    }

    protected Response processRegistration(boolean action, String execution, AuthenticationSessionModel authSession, String errorMessage) {
        return processFlow(action, execution, authSession, REGISTRATION_PATH, CustomAuthenticationFlowResolver.resolveRegistrationFlow(authSession), errorMessage, new AuthenticationProcessor());
    }

    AuthenticationSessionModel createAuthenticationSessionForClient(String clientId) throws UriBuilderException, IllegalArgumentException {
        AuthenticationSessionModel authSession;

        ClientModel client = clientId != null ? realm.getClientByClientId(clientId) : SystemClientUtil.getSystemClient(realm);

        RootAuthenticationSessionModel rootAuthSession = new AuthenticationSessionManager(session).createAuthenticationSession(realm, true);
        authSession = rootAuthSession.createAuthenticationSession(client);

        authSession.setAction(AuthenticationSessionModel.Action.AUTHENTICATE.name());
        //authSession.setNote(AuthenticationManager.END_AFTER_REQUIRED_ACTIONS, "true");
        authSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        String redirectUri = Urls.accountBase(session.getContext().getUri().getBaseUri()).path("/").build(realm.getName()).toString();
        authSession.setRedirectUri(redirectUri);
        authSession.setClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM, OAuth2Constants.CODE);
        authSession.setClientNote(OIDCLoginProtocol.REDIRECT_URI_PARAM, redirectUri);
        authSession.setClientNote(OIDCLoginProtocol.ISSUER, Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName()));

        return authSession;
    }

    protected Response processFlow(boolean action, String execution, AuthenticationSessionModel authSession, String flowPath, AuthenticationFlowModel flow, String errorMessage, AuthenticationProcessor processor) {
        processor.setAuthenticationSession(authSession)
                .setFlowPath(flowPath)
                .setBrowserFlow(true)
                .setFlowId(flow.getId())
                .setConnection(clientConnection)
                .setEventBuilder(event)
                .setRealm(realm)
                .setSession(session)
                .setUriInfo(session.getContext().getUri())
                .setRequest(request);
        if (errorMessage != null) {
            processor.setForwardedErrorMessage(new FormMessage(null, errorMessage));
        }

        // Check the forwarded error message, which was set by previous HTTP request
        String forwardedErrorMessage = authSession.getAuthNote(FORWARDED_ERROR_MESSAGE_NOTE);
        if (forwardedErrorMessage != null) {
            authSession.removeAuthNote(FORWARDED_ERROR_MESSAGE_NOTE);
            processor.setForwardedErrorMessage(new FormMessage(null, forwardedErrorMessage));
        }

        Response response;
        try {
            if (action) {
                response = processor.authenticationAction(execution);
            } else {
                response = processor.authenticate();
            }
        } catch (WebApplicationException e) {
            response = e.getResponse();
            authSession = processor.getAuthenticationSession();
        } catch (Exception e) {
            response = processor.handleBrowserException(e);
            authSession = processor.getAuthenticationSession(); // Could be changed (eg. Forked flow)
        }

        return BrowserHistoryHelper.getInstance().saveResponseAndRedirect(session, authSession, response, action, request);
    }

}
