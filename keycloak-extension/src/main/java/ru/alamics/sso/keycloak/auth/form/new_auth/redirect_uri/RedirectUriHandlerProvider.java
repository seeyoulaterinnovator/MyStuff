package ru.alamics.sso.keycloak.auth.form.new_auth.redirect_uri;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import static ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil.*;

@Slf4j
public class RedirectUriHandlerProvider implements Authenticator {

    private KeycloakSession session;

    public RedirectUriHandlerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        log.info("RedirectUriHandlerProvider");
        AuthenticationSessionModel sessionModel = context.getAuthenticationSession();
        decideResponseFormat(context, sessionModel);
        UserSessionModel userSessionModel = isUserAuthenticated(context, sessionModel);

        if (decideResponse(context, sessionModel, userSessionModel, session)) {
            context.success();
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        context.success();
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }
}
