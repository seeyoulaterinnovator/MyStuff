package ru.alamics.sso.keycloak.mobile.resource;

import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionTokenHandler;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.models.utils.SystemClientUtil;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.resources.SessionCodeChecks;
import org.keycloak.services.util.BrowserHistoryHelper;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilderException;
import java.io.IOException;
import java.util.Map;

import static org.jboss.resteasy.spi.ResteasyProviderFactory.getContextData;

public class RestResource {

    public static final String RESET_CREDENTIALS_PATH = "reset-credentials";
    public static final String POST_BROKER_LOGIN_PATH = "post-broker-login";
    public static final String FORWARDED_ERROR_MESSAGE_NOTE = "forwardedErrorMessage";
    public static final String SESSION_CODE = "session_code";
    public static final String AUTH_SESSION_ID = "auth_session_id";
    private final KeycloakSession session;
    private final RealmModel realm;
    private final ClientConnection clientConnection;

    private final EventBuilder event;
    private final HttpRequest request;

    RestResource(KeycloakSession session) {
        this.session = session;

        KeycloakContext context = session.getContext();
        this.realm = context.getRealm();

        this.clientConnection = session.getContext().getConnection();

        this.event = new EventBuilder(realm, session, clientConnection);

        this.request = getContextData(HttpRequest.class);
    }

    @Path(RESET_CREDENTIALS_PATH)
    @POST
    public Response resetCred(@QueryParam(AUTH_SESSION_ID) String authSessionId, // optional, can get from cookie instead
                              @QueryParam(SESSION_CODE) String code,
                              @QueryParam(Constants.EXECUTION) String execution,
                              @QueryParam(Constants.CLIENT_ID) String clientId,
                              @QueryParam(Constants.TAB_ID) String tabId) throws IOException {

        Response response = createFlow(clientId, tabId, code);

        if (Response.Status.BAD_REQUEST.equals(response.getStatusInfo()) || Response.Status.NOT_FOUND.equals(response.getStatusInfo())){
            return response;
        }

        Map<String, String> entity = (Map<String, String>) response.getEntity();
        String ex = entity.get("execution");
        String accessCode = entity.get("access_code");
        String sessionState = entity.get("session_state");
        String tab = entity.get("tab_id");

        event.event(EventType.RESET_PASSWORD);

        return resetCredentials(sessionState, accessCode, ex, clientId, tab);
    }

    private Response createFlow(String clientId, String tabId, String code){
        ClientModel client = realm.getClientByClientId(clientId);
        AuthenticationSessionModel authSession = new AuthenticationSessionManager(session).getCurrentAuthenticationSession(realm, client, tabId);
        if (authSession == null && code == null) {
            if (!realm.isResetPasswordAllowed()) {
                event.event(EventType.RESET_PASSWORD);
                event.error(Errors.NOT_ALLOWED);
                return ErrorResponse.error(Messages.RESET_CREDENTIAL_NOT_ALLOWED, Response.Status.BAD_REQUEST);
            }
            return processResetCredentials(false, null, createAuthenticationSessionForClient(), null);
        }
        return ErrorResponse.error("Не удалось запусть flow", Response.Status.NOT_FOUND);
    }

    protected Response processResetCredentials(boolean actionRequest, String execution, AuthenticationSessionModel authSession, String errorMessage) {
        AuthenticationProcessor authProcessor = new ResetCredentialsActionTokenHandler.ResetCredsAuthenticationProcessor();

        return processFlow(actionRequest, execution, authSession, RESET_CREDENTIALS_PATH, realm.getResetCredentialsFlow(), errorMessage, authProcessor);
    }

    AuthenticationSessionModel createAuthenticationSessionForClient() throws UriBuilderException, IllegalArgumentException {
        AuthenticationSessionModel authSession;

        // set up the account service as the endpoint to call.
        ClientModel client = SystemClientUtil.getSystemClient(realm);

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

    protected Response resetCredentials(String authSessionId, String code, String execution, String clientId, String tabId) {
        SessionCodeChecks checks = checksForCode(authSessionId, code, execution, clientId, tabId, RESET_CREDENTIALS_PATH);
        if (!checks.verifyActiveAndValidAction(AuthenticationSessionModel.Action.AUTHENTICATE.name(), ClientSessionCode.ActionType.USER)) {
            return checks.getResponse();
        }
        final AuthenticationSessionModel authSession = checks.getAuthenticationSession();

        return processResetCredentials(checks.isActionRequest(), execution, authSession, null);
    }

    private SessionCodeChecks checksForCode(String authSessionId, String code, String execution, String clientId, String tabId, String flowPath) {
        SessionCodeChecks res = new SessionCodeChecks(realm, session.getContext().getUri(), request, clientConnection, session, event, authSessionId, code, execution, clientId, tabId, flowPath);
        res.initialVerify();
        return res;
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
