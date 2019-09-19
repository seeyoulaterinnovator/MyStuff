package ru.alamics.sso.keycloak.resetcred;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.auth.AuthBaseClass;
import ru.alamics.sso.keycloak.auth.UserFind;
import ru.alamics.sso.keycloak.resetcred.factory.ResetFactory;
import ru.alamics.sso.keycloak.resetcred.factory.ResetFactoryImpl;
import ru.alamics.sso.keycloak.resetcred.type.ResetType;

import java.util.Collections;


@Slf4j
public class ResetCredentialEmailOrPhone extends AuthBaseClass {

    private KeycloakSession session;

    ResetCredentialEmailOrPhone (KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void authenticate (AuthenticationFlowContext context) {
        var user = context.getUser();
        var resetType = ResetType.EMAIL;
        var authenticationSession = context.getAuthenticationSession();
        var username = authenticationSession.getAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME);

        if(user == null) {
            username = username.replaceAll("\\D",  "");
            var userFind = new UserFind(this.session);
            user = userFind.getUserByPhone(username);
            username = user.getUsername();
            authenticationSession.setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, user.getEmail());
            context.getHttpRequest().getDecodedFormParameters().replace("username", Collections.singletonList(user.getEmail()));
        }

        authenticationSession.setAuthNote("RESET_TYPE", resetType.name());
        ResetFactory factory = new ResetFactoryImpl(this.session, context);
        ResetCredential resetCredential = factory.create(resetType);
        resetCredential.reset(user, username);
    }


    @Override
    public void action (AuthenticationFlowContext context) {
        context.getUser().setEmailVerified(true);
        context.success();
    }

}
