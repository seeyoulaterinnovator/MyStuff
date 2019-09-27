package ru.alamics.sso.keycloak.resetcred;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import ru.alamics.sso.keycloak.auth.AuthBaseClass;
import ru.alamics.sso.keycloak.auth.UserFind;
import ru.alamics.sso.keycloak.resetcred.factory.ResetFactory;
import ru.alamics.sso.keycloak.resetcred.factory.ResetFactoryImpl;
import ru.alamics.sso.keycloak.resetcred.type.ResetType;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.rias.RiasService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collections;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;


@Slf4j
public class ResetCredentialEmailOrPhone extends AuthBaseClass {

    private KeycloakSession session;
    private final RiasService riasService;

    ResetCredentialEmailOrPhone (KeycloakSession session) {
        this.session = session;
        try {
            InitialContext context = new InitialContext();
            riasService = (RiasService) context.lookup("java:global/domru-sso/" + RiasService.class.getSimpleName());
            log.info("Got riasService from context");
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
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
        UserModel userModel = context.getUser();
        if (userModel == null) {
            return false;
        }
        User.UserBuilder userBuilder = User.builder().email(userModel.getEmail());
        if (!userModel.getAttribute(ATTR_PHONE_NAME).isEmpty()){
            userBuilder.phone(userModel.getAttribute(ATTR_PHONE_NAME).get(0));
        }
        User user = userBuilder.build();
        if (!riasService.checkEmail(user) && !riasService.checkPhone(user)) {
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
