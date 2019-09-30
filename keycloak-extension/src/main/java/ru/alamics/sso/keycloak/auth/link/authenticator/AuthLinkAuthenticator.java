package ru.alamics.sso.keycloak.auth.link.authenticator;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.actiontoken.DefaultActionTokenKey;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

@Slf4j
public class AuthLinkAuthenticator implements Authenticator {

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        log.info("authenticate");
        try {
            String actionTokenUserId = context.getAuthenticationSession().getAuthNote(DefaultActionTokenKey.ACTION_TOKEN_USER_ID);
            if (actionTokenUserId != null) {
                UserModel existingUser = context.getSession().users().getUserById(actionTokenUserId, context.getRealm());

                // Action token logics handles checks for user ID validity and user being enabled

                log.info(String.format("authentication via action token. Skipping screen and using user '%s' ", existingUser.getUsername()));
                context.setUser(existingUser);
                context.success();
                return;
            }
        } catch (Exception e) {
            log.error("", e);
        }

        log.info("authenticate done");
    }

    @Override
    public void action(AuthenticationFlowContext context) {

        log.info("action");
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
