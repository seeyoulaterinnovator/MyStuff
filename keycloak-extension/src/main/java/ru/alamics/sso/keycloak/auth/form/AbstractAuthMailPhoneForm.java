package ru.alamics.sso.keycloak.auth.form;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import ru.alamics.sso.jpa.entity.common.BlockType;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.stats.LoginHistory;
import ru.alamics.sso.user.UserAttributeService;
import ru.alamics.sso.util.Util;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.util.Collections;

import static ru.alamics.sso.registration.model.UserConstants.AUTH_FORM_SUCCESS;

@Slf4j
public abstract class AbstractAuthMailPhoneForm extends AbstractUsernameFormAuthenticator implements Authenticator {

    private final UserFindService userFindService;

    private final LoginHistory loginHistory;
    private UserAttributeService attributeService;

    public AbstractAuthMailPhoneForm(UserFindService userFindService) {
        this.userFindService = userFindService;
        this.loginHistory = Lookup.lookup(LoginHistory.class);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }
        if (!validateUserAndPassword(context, formData)) {
            return;
        }
        context.getAuthenticationSession().setAuthNote(AUTH_FORM_SUCCESS, Util.TRUE_STR);
        context.success();
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
        String loginHint = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);
        String rememberMeUsername = AuthenticationManager.getRememberMeUsername(context.getRealm(), context.getHttpRequest().getHttpHeaders());

        if (loginHint != null) {
            formData.add(AuthenticationManager.FORM_USERNAME, loginHint);
        } else if (rememberMeUsername != null) {
            formData.add(AuthenticationManager.FORM_USERNAME, rememberMeUsername);
            formData.add("rememberMe", "on");
        }
        context.challenge(challenge(context, formData));
    }

    @Override
    public boolean validateUserAndPassword(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        String username = inputData.getFirst(AuthenticationManager.FORM_USERNAME);
        if (username == null) {
            context.getEvent().error(Errors.USER_NOT_FOUND);
            Response challengeResponse = challenge(context, Messages.INVALID_USER);
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return false;
        }

        // remove leading and trailing whitespace
        username = username.trim();

        context.getEvent().detail(Details.USERNAME, username);
        context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

        UserModel user = null;
        try {
            log.info("find user casual");
            user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);

            if (user == null) {
                log.info("find user by phone");
                user = Util.getUserAdapter(context.getSession(), userFindService.getUserByPhone(context.getRealm(), username));
            }

            log.info("user is " + user);
            if (user != null) {
                log.info(user.getId());
            }
            if (!isSuccessCheckUser(context, user)) {
                return false;
            }

        } catch (ModelDuplicateException mde) {
            ServicesLogger.LOGGER.modelDuplicateException(mde);

            // Could happen during federation import
            if (mde.getDuplicateFieldName() != null && mde.getDuplicateFieldName().equals(UserModel.EMAIL)) {
                setDuplicateUserChallenge(context, Errors.EMAIL_IN_USE, Messages.EMAIL_EXISTS, AuthenticationFlowError.INVALID_USER);
            } else {
                setDuplicateUserChallenge(context, Errors.USERNAME_IN_USE, Messages.USERNAME_EXISTS, AuthenticationFlowError.INVALID_USER);
            }

            return false;
        }

        if (invalidUser(context, user)) {
            return false;
        }

        if (!validatePassword(context, user, inputData)) {
            return false;
        }
        if(user.getAttribute(BlockType.MANAGER_BLOCK.getType()).isEmpty()) {
            user.setEnabled(true);

            attributeService = new UserAttributeService(context.getSession(), userFindService);
            attributeService.deleteAttributes(user.getId(), Collections.singletonList(BlockType.SYSTEM_BLOCK.getType()));
        }

        if (!enabledUser(context, user)) {
            return false;
        }

        String rememberMe = inputData.getFirst("rememberMe");
        boolean remember = rememberMe != null && rememberMe.equalsIgnoreCase("on");
        if (remember) {
            context.getAuthenticationSession().setAuthNote(Details.REMEMBER_ME, "true");
            context.getEvent().detail(Details.REMEMBER_ME, "true");
        } else {
            context.getAuthenticationSession().removeAuthNote(Details.REMEMBER_ME);
        }
        context.setUser(user);
        if (user.getRequiredActions().isEmpty()) {
            UserEntity userEntity = new UserEntity();
            userEntity.setId(user.getId());
            loginHistory.createSuccessAuth(userEntity, context.getRealm().getName());
        }
        return true;
    }

    public abstract boolean isSuccessCheckUser(AuthenticationFlowContext context, UserModel user);

    @Override
    public boolean requiresUser() {
        return false;
    }

    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = context.form();

        if (formData.size() > 0) forms.setFormData(formData);

        return forms.createLogin();
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    @Override
    public void close() {

    }
}
