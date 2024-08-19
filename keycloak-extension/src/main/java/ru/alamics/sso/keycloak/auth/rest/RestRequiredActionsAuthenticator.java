package ru.alamics.sso.keycloak.auth.rest;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.common.ClientConnection;
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
import ru.alamics.sso.registration.model.UserConstants;
import ru.alamics.sso.util.Util;
import ru.alamics.sso.jpa.util.CollectionUtils;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Slf4j
public class RestRequiredActionsAuthenticator extends AbstractAuthenticator {

    private final KeycloakSession session;
    private final RealmModel realm;
    private EventBuilder event;

    @Context
    private ClientConnection clientConnection;

    @Context
    private HttpRequest request;

    public RestRequiredActionsAuthenticator(KeycloakSession session) {
        this.session = session;
        this.realm = session.getContext().getRealm();
    }

    public void init() {
        event = new EventBuilder(realm, session, clientConnection);
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
        context.getAuthenticationSession()
                .removeAuthNote(UserConstants.AUTH_NOTE_REST_SMS_OR_PHONE_CALL_END_REQUIRED_ACTION);
        Object entity;
        try {
            AuthenticationSessionModel authSession = context.getAuthenticationSession();
            authSession.setClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM, OIDCResponseType.NONE);
            authSession.setRedirectUri(""); //костыль, redirect url в REST не используем, при null падает NPE
            Response response = AuthenticationManager.nextActionAfterAuthentication(session, authSession, clientConnection, request, session.getContext().getUri(), event);
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
            context.getUser().addRequiredAction(UserModel.RequiredAction.valueOf(requiredAction));
            throw e;
        }
        if(requiredAction != null) {
            context.getUser().addRequiredAction(UserModel.RequiredAction.valueOf(requiredAction));
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
    }

    @Override
    public void action(AuthenticationFlowContext context) {
    }
}
