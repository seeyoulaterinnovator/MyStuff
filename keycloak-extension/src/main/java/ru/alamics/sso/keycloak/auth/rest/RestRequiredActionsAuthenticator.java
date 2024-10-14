package ru.alamics.sso.keycloak.auth.rest;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.utils.OIDCResponseType;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticator;
import ru.alamics.sso.keycloak.auth.form.new_auth.newAuthReqActions.EmailReqAction;
import ru.alamics.sso.registration.model.UserConstants;
import ru.alamics.sso.util.Util;

import java.util.List;
import java.util.Map;

import static ru.alamics.sso.registration.model.UserConstants.AUTH_NOTE_DIRECT_GRANT_SESSION_CLEAR_DISABLED;

@Slf4j
public class RestRequiredActionsAuthenticator extends AbstractAuthenticator {
    private static final List<String> DIRECT_GRANT_ALLOWED_REQUIRED_ACTIONS = List.of(
            UserModel.RequiredAction.UPDATE_PASSWORD.name(),
            UserModel.RequiredAction.UPDATE_PROFILE.name(),
            UserModel.RequiredAction.VERIFY_EMAIL.name(),
            EmailReqAction.PROVIDER_ID
    );

    private final KeycloakSession session;
    private final RealmModel realm;
    private final EventBuilder event;

    public RestRequiredActionsAuthenticator(KeycloakSession session) {
        this.session = session;
        this.realm = session.getContext().getRealm();
        this.event = new EventBuilder(realm, session, session.getContext().getConnection());
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        if (!Util.isPasswordGrandType(session)) {
            context.attempted();
            return;
        }
        String requiredAction = context.getAuthenticationSession().getAuthNote(
                UserConstants.AUTH_NOTE_REST_SMS_OR_PHONE_CALL_END_REQUIRED_ACTION
        );
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        UserModel user = context.getUser();
        authSession.removeAuthNote(UserConstants.AUTH_NOTE_REST_SMS_OR_PHONE_CALL_END_REQUIRED_ACTION);
        Object entity;
        try {
            authSession.setClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM, OIDCResponseType.NONE);
            authSession.setRedirectUri(""); //костыль, redirect url в REST не используем, при null падает NPE
            Response response = AuthenticationManager.nextActionAfterAuthentication(
                    session,
                    authSession,
                    context.getConnection(),
                    context.getHttpRequest(),
                    session.getContext().getUri(),
                    event
            );
            entity = response.getEntity();
            if (!(entity instanceof AccessTokenResponse)) {
                if (!(entity instanceof Map)) {
                    context.attempted();
                    return;
                }
                String execution = ((Map<String, String>) entity).get("execution");
                if (Util.isEmpty(execution)) {
                    context.failure(AuthenticationFlowError.FORK_FLOW);
                    return;
                }
                context.getExecution().setId(execution); //костыль, возможно можно лучше
                ClientSessionCode<AuthenticationSessionModel> accessCode = new ClientSessionCode<>(session, realm, authSession);
                accessCode.setAction(AuthenticationSessionModel.Action.REQUIRED_ACTIONS.name());
                authSession.setAuthNote(AuthenticationProcessor.CURRENT_FLOW_PATH, LoginActionsService.REQUIRED_ACTION);
                authSession.setAuthNote(AuthenticationProcessor.CURRENT_AUTHENTICATION_EXECUTION, execution);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if(requiredAction != null) {
                user.addRequiredAction(UserModel.RequiredAction.valueOf(requiredAction));
            }
            throw e;
        }
        if(requiredAction != null) {
            user.addRequiredAction(UserModel.RequiredAction.valueOf(requiredAction));
            context.failure(
                    AuthenticationFlowError.CREDENTIAL_SETUP_REQUIRED,
                    Response.status(Response.Status.OK)
                            .entity(entity)
                            .type(org.keycloak.utils.MediaType.APPLICATION_JSON_TYPE)
                            .build()

            );
        } else {
            context.challenge(Response.ok(
                    entity,
                    MediaType.APPLICATION_JSON_TYPE
            ).build());
        }
        if(user.getRequiredActionsStream().anyMatch(DIRECT_GRANT_ALLOWED_REQUIRED_ACTIONS::contains)) {
            authSession.setAuthNote(AUTH_NOTE_DIRECT_GRANT_SESSION_CLEAR_DISABLED, Boolean.TRUE.toString());
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) { }
}
