package ru.alamics.sso.keycloak.resetcred;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.auth.AuthBaseClass;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.resetcred.factory.ResetFactory;
import ru.alamics.sso.keycloak.resetcred.factory.ResetFactoryImpl;
import ru.alamics.sso.keycloak.resetcred.type.ResetType;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.rias.exception.RiasCheckException;
import ru.alamics.sso.registration.rias.port.RiasApiService;
import ru.alamics.sso.registration.service.UserFindService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collections;


@Slf4j
public class ResetCredentialEmailOrPhone extends AuthBaseClass {

    private static final String RESET_CREDENTIALS_REDIRECT_URL = "reset.credentials.redirect.url";

    private KeycloakSession session;
    private RiasApiService riasApiService;
    private UserFindService userFindService;
    private ApplicationProperties properties;

    ResetCredentialEmailOrPhone(KeycloakSession session) {
        this.session = session;

        riasApiService = (RiasApiService) Lookup.lookup(RiasApiService.class);
        log.info("Got riasService from context");

        userFindService = (UserFindService) Lookup.lookup(UserFindService.class);
        log.info("Got userFindService from context");

        properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        var user = context.getUser();
        var resetType = ResetType.EMAIL;
        var authenticationSession = context.getAuthenticationSession();
        var username = authenticationSession.getAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME);

        if (user == null && username.startsWith("+7")) {
            username = username.replaceAll("\\D", "");
            var userFind = userFindService.getUserByPhone(context.getRealm(), username);
            if (userFind != null) {
                user = context.getSession().users().getUserById(userFind.getId(), context.getSession().realms().getRealm(userFind.getRealmId()));
                username = userFind.getUsername();
                authenticationSession.setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, userFind.getEmail());
                context.getHttpRequest().getDecodedFormParameters().replace("username", Collections.singletonList(userFind.getEmail()));
            }
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
    public void action(AuthenticationFlowContext context) {
        context.getUser().setEmailVerified(true);
        context.success();
    }

    private boolean checkRias(AuthenticationFlowContext context) {
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        String username = authenticationSession.getAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME);

        try {
            if (username == null)
                return false;

            if (username.startsWith("+7")) {
                username = username.replaceAll("\\D", "");
            }
            if (!riasApiService.checkParam(username)) {
                return false;
            }
        } catch (RiasCheckException rce) {
            log.error("RIAS check service", rce);
            return false;
        }

        String location = properties.getProperty(RESET_CREDENTIALS_REDIRECT_URL);
        if (location == null)
            location = "https://lkb2b.domru.ru/recovery";

        URI uriLoc = UriBuilder.fromPath(location).build();

        Response response = Response.seeOther(uriLoc)
                .build();

        log.debug("Redirecting to {}", location);
        context.forceChallenge(response);

        return true;
    }
}
