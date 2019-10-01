package ru.alamics.sso.keycloak.auth.link.authenticator;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.TokenVerifier;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.actiontoken.DefaultActionTokenKey;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.sessions.AuthenticationSessionModel;

@Slf4j
@Deprecated
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

                //AuthenticationSessionModel clientSession = context.getAuthenticationSession();
                //LoginProtocol protocol = context.getSession().getProvider(LoginProtocol.class, clientSession.getProtocol());

                String tokenString = context.getUriInfo().getQueryParameters().getFirst("key");

                TokenVerifier<DefaultActionTokenKey> tokenVerifier = TokenVerifier.create(tokenString, DefaultActionTokenKey.class);
                DefaultActionTokenKey aToken = tokenVerifier.getToken();

                // get AuthLinkActionToken
                // aToken.get asid -> context.attachUserSession();

                context.getSession().setAttribute(AuthenticationManager.SSO_AUTH, "true");

                //context.setUser(authResult.getUser());
                //context.attachUserSession(authResult.getSession());


                context.setUser(existingUser);
                context.success();
                return;
            } else {
                context.attempted();
            }
        } catch (Exception e) {
            log.error("", e);
            context.attempted();
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
