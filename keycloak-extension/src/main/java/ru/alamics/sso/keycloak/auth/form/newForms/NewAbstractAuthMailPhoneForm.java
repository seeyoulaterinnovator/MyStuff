package ru.alamics.sso.keycloak.auth.form.newForms;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.auth.form.AbstractAuthMailPhoneForm;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.util.Util;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;


import static ru.alamics.sso.registration.model.UserConstants.AUTH_FORM_SUCCESS;


@Slf4j
public abstract class NewAbstractAuthMailPhoneForm extends AbstractAuthMailPhoneForm {
    private final UserFindService userFindService;

    public NewAbstractAuthMailPhoneForm(UserFindService userFindService) {
        super(userFindService);
        this.userFindService = userFindService;
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

            if (!(username.matches("^\\d+$")) && context.getHttpRequest().getDecodedFormParameters().containsKey("loginPasswordButton")) {
                if (!isUserNameValid(username, context, "email")) return false;
                user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);
            } else {
                if (!isUserNameValid(username, context, "phone")) return false;
                user = Util.getUserAdapter(context.getSession(), userFindService.getUserByPhone(context.getRealm(), username));
            }

            if (user != null) {
                log.info("user is " + user);
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


        if (context.getHttpRequest().getDecodedFormParameters().containsKey("loginPasswordButton")) {
            if (!validatePassword(context, user, inputData)) {
                return false;
            }
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
        return true;
    }

    private boolean isUserNameValid(String username, AuthenticationFlowContext context, String type) {
        String phoneRegex = "^79\\d{2}\\d{7}$";
        String emailRegex = "^[a-zA-Z\\d_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z\\d.-]+$";

        switch (type) {
            case "email":
                if (!(username.matches(emailRegex))) {
                    context.form().setAttribute("error", "incorrect mail");
                    authenticate(context);
                    return false;
                }
                break;
            case "phone":
                if (!(username.matches(phoneRegex))) {
                    context.form().setAttribute("error", "incorrect phone");
                    authenticate(context);
                    return false;
                }
        }
        return true;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }
        context.form().setAttribute("isSwitcherOn", getCurrentSwitcherStatus(context.getHttpRequest(), context));
        if (!validateUserAndPassword(context, formData)) {
            return;
        }
        SsoUtil.sendEmailVer(context);
        context.getAuthenticationSession().setAuthNote(AUTH_FORM_SUCCESS, Util.TRUE_STR);
        context.form().setAttribute("isVerified", context.getUser().isEmailVerified());
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
        context.form().setAttribute("isSwitcherOn", getCurrentSwitcherStatus(context.getHttpRequest(), context));
        MultivaluedMap<String, String> multivaluedMap = context.getHttpRequest().getDecodedFormParameters();
        HttpRequest httpRequest = context.getHttpRequest();
        HttpHeaders h = httpRequest.getHttpHeaders();

        context.challenge(challenge(context, formData));
    }

    protected Boolean getCurrentSwitcherStatus(HttpRequest httpRequest, AuthenticationFlowContext context) {

        final String loginPasswordAuthNote = "loginPassword";
        final String loginSmsAuthNote = "loginSms";
        final String loginPhoneCallAuthNote = "loginPhoneCall";

        boolean isOff = httpRequest.getDecodedFormParameters().containsKey("off");
        boolean isOn = httpRequest.getDecodedFormParameters().containsKey("on");

        boolean isLoginPassword = context.getRealm().getAttribute("loginViaEmailOrUsernameAndPassword", false);
        boolean isSms = context.getRealm().getAttribute("loginViaSms", false);
        boolean isPhoneCall = context.getRealm().getAttribute("loginViaPhoneCall", false);
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();


        if (!(isOff || isOn)) {
            if (isLoginPassword) {
                if (authenticationSession.getAuthNote(loginSmsAuthNote) == null) {
                    authenticationSession.setAuthNote(loginPasswordAuthNote, loginPasswordAuthNote);
                    return false;
                } else {
                    authenticationSession.removeAuthNote(loginPasswordAuthNote);
                    return true;
                }
            } else if (isSms) {
                if (authenticationSession.getAuthNote(loginPasswordAuthNote) == null) {
                    authenticationSession.setAuthNote(loginSmsAuthNote, loginSmsAuthNote);
                    return false;
                } else {
                    authenticationSession.removeAuthNote(loginSmsAuthNote);
                    return true;
                }
            } else if (isPhoneCall) {
                if (authenticationSession.getAuthNote(loginPasswordAuthNote) == null) {
                    authenticationSession.setAuthNote(loginPhoneCallAuthNote, loginPhoneCallAuthNote);
                    return false;
                } else {
                    authenticationSession.removeAuthNote(loginPhoneCallAuthNote);
                    return true;
                }
            }
        } else if (isOn) {
            if (!isLoginPassword) {
                authenticationSession.setAuthNote(loginPasswordAuthNote, loginPasswordAuthNote);
                if (isSms) {
                    authenticationSession.removeAuthNote(loginSmsAuthNote);

                } else {
                    authenticationSession.removeAuthNote(loginPhoneCallAuthNote);
                }
            } else {
                authenticationSession.setAuthNote(loginSmsAuthNote, loginSmsAuthNote);
                authenticationSession.removeAuthNote(loginPasswordAuthNote);
            }
            return true;
        } else {
            if (isLoginPassword) {
                authenticationSession.setAuthNote(loginPasswordAuthNote, loginPasswordAuthNote);
                authenticationSession.removeAuthNote(loginSmsAuthNote);
            } else if (isSms) {
                authenticationSession.setAuthNote(loginSmsAuthNote, loginSmsAuthNote);
                authenticationSession.removeAuthNote(loginPasswordAuthNote);

            } else {
                authenticationSession.setAuthNote(loginPhoneCallAuthNote, loginPhoneCallAuthNote);
                authenticationSession.removeAuthNote(loginPasswordAuthNote);
            }
            return false;
        }
        return null;
    }


    public abstract boolean isSuccessCheckUser(AuthenticationFlowContext context, UserModel user);


}

