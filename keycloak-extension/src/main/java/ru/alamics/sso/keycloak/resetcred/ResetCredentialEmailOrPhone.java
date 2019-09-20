package ru.alamics.sso.keycloak.resetcred;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.auth.AuthBaseClass;
import ru.alamics.sso.keycloak.auth.UserFind;
import ru.alamics.sso.keycloak.resetcred.factory.ResetFactory;
import ru.alamics.sso.keycloak.resetcred.factory.ResetFactoryImpl;
import ru.alamics.sso.keycloak.resetcred.type.ResetType;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collections;


@Slf4j
public class ResetCredentialEmailOrPhone extends AuthBaseClass {

    private KeycloakSession session;
//private final RiasService riasService;

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

        if (checkRias(context)) {
            return;
        } else {
            context.forkWithSuccessMessage(new FormMessage(Messages.EMAIL_SENT));
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

    private boolean checkRias(AuthenticationFlowContext context) {
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        String username = authenticationSession.getAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME);

        if (username == null || !username.contains("rias")) {
            return false;
        }

        String location = "https://lkb2b.domru.ru/recovery";

        URI uriLoc = UriBuilder.fromPath(location).build();

        Response response = Response.seeOther(uriLoc)
                .build();

        log.debug("Redirecting to {}", location);
        context.forceChallenge(response);

        return true;
    }
}
