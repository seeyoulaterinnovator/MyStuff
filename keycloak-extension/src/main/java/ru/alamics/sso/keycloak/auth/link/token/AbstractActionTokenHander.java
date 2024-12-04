package ru.alamics.sso.keycloak.auth.link.token;

import org.keycloak.Config;
import org.keycloak.authentication.actiontoken.ActionTokenContext;
import org.keycloak.authentication.actiontoken.ActionTokenHandler;
import org.keycloak.authentication.actiontoken.ActionTokenHandlerFactory;
import org.keycloak.authentication.actiontoken.DefaultActionToken;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.sessions.AuthenticationSessionModel;

public abstract class AbstractActionTokenHander<T extends JsonWebToken> implements ActionTokenHandler<T>, ActionTokenHandlerFactory<T> {

    private final String id;
    private final Class<T> tokenClass;
    private final String defaultErrorMessage;
    private final EventType defaultEventType;
    private final String defaultEventError;

    public AbstractActionTokenHander(String id, Class<T> tokenClass, String defaultErrorMessage, EventType defaultEventType, String defaultEventError) {
        this.id = id;
        this.tokenClass = tokenClass;
        this.defaultErrorMessage = defaultErrorMessage;
        this.defaultEventType = defaultEventType;
        this.defaultEventError = defaultEventError;
    }

    @Override
    public ActionTokenHandler<T> create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void close() {
    }

    @Override
    public Class<T> getTokenClass() {
        return this.tokenClass;
    }

    @Override
    public EventType eventType() {
        return this.defaultEventType;
    }

    @Override
    public String getDefaultErrorMessage() {
        return this.defaultErrorMessage;
    }

    @Override
    public String getDefaultEventError() {
        return this.defaultEventError;
    }

    @Override
    public String getAuthenticationSessionIdFromToken(T token, ActionTokenContext<T> tokenContext, AuthenticationSessionModel currentAuthSession) {
        return token instanceof DefaultActionToken ? ((DefaultActionToken) token).getCompoundAuthenticationSessionId() : null;
    }

    @Override
    public AuthenticationSessionModel startFreshAuthenticationSession(T token, ActionTokenContext<T> tokenContext) {
        AuthenticationSessionModel authSession = tokenContext.createAuthenticationSessionForClient(token.getIssuedFor());
        authSession.setAuthNote(AuthenticationManager.END_AFTER_REQUIRED_ACTIONS, "true");
        return authSession;
    }

    @Override
    public boolean canUseTokenRepeatedly(T token, ActionTokenContext<T> tokenContext) {
        return true;
    }
}
