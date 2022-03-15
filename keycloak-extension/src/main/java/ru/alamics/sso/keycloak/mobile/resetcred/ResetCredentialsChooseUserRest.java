package ru.alamics.sso.keycloak.mobile.resetcred;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.actiontoken.DefaultActionTokenKey;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticator;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.user.UserServiceUtil;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Collections;

public class ResetCredentialsChooseUserRest extends AbstractAuthenticator {

    private static final Logger logger = Logger.getLogger(ResetCredentialsChooseUserRest.class);

    private final UserFindService userFindService;

    public ResetCredentialsChooseUserRest() {

        this.userFindService = Lookup.lookup(UserFindService.class);

    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String existingUserId = context.getAuthenticationSession().getAuthNote(AbstractIdpAuthenticator.EXISTING_USER_INFO);
        if (existingUserId != null) {
            UserModel existingUser = AbstractIdpAuthenticator.getExistingUser(context.getSession(), context.getRealm(), context.getAuthenticationSession());

            logger.debugf("Forget-password triggered when reauthenticating user after first broker login. Skipping reset-credential-choose-user screen and using user '%s' ", existingUser.getUsername());
            context.setUser(existingUser);
            context.success();
            return;
        }

        String actionTokenUserId = context.getAuthenticationSession().getAuthNote(DefaultActionTokenKey.ACTION_TOKEN_USER_ID);
        if (actionTokenUserId != null) {
            UserModel existingUser = context.getSession().users().getUserById(actionTokenUserId, context.getRealm());

            // Action token logics handles checks for user ID validity and user being enabled

            logger.debugf("Forget-password triggered when reauthenticating user after authentication via action token. Skipping reset-credential-choose-user screen and using user '%s' ", existingUser.getUsername());
            context.setUser(existingUser);
            context.success();
            return;
        }

        context.challenge(createForm(context));
    }

    private Response createForm(AuthenticationFlowContext context) {
        LoginFormsProvider form = context.form();
        return form.createForm("login-reset-password.ftl");
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        EventBuilder event = context.getEvent();
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        String username = formData.getFirst("username");
        if (username == null || username.isEmpty()) {
            event.error(Errors.USERNAME_MISSING);
            Response challenge = JsonResponse.fail().message(Errors.USERNAME_MISSING).build();
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challenge);
            return;
        }

        UserEntity userFind = null;

        username = username.trim();

        RealmModel realm = context.getRealm();
        UserModel user = context.getSession().users().getUserByUsername(username, realm);
        if (user == null && realm.isLoginWithEmailAllowed() && username.contains("@")) {
            user = context.getSession().users().getUserByEmail(username, realm);
        }

        if (user == null && username.startsWith("+7")) {
            userFind = findUserByConvertUsernameToPhone(realm, username);
            if (userFind != null && userFind.isEnabled()) {
                user = context.getSession().users().getUserById(userFind.getId(), context.getSession().realms().getRealm(userFind.getRealmId()));
                username = userFind.getUsername();
                authenticationSession.setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, userFind.getEmail());
                context.getHttpRequest().getDecodedFormParameters().replace("username", Collections.singletonList(userFind.getEmail()));
            }
        }

        authenticationSession.setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

        if (user == null && userFind == null) {
            Response challenge = JsonResponse.fail().message("Пользователь не найден").build();
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challenge);
            return;
        }

        if (userFind != null && !userFind.isEnabled() || user != null && !user.isEnabled()) {
            Response challenge = JsonResponse.fail().message("Пользователь заблокирован").build();
            context.failureChallenge(AuthenticationFlowError.USER_DISABLED, challenge);
            return;
        }

        context.setUser(user);

        authenticationSession.setAuthNote("MP", "grant_type");

        context.success();

    }

    private UserEntity findUserByConvertUsernameToPhone(RealmModel realm, final String username) {

        String phone = UserServiceUtil.doCleanPhoneStartWithSeven(username);

        return phone == null ? null : userFindService.getUserByPhone(realm, phone);
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

}
