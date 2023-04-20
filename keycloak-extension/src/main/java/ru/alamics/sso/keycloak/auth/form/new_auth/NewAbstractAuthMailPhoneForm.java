package ru.alamics.sso.keycloak.auth.form.new_auth;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.auth.form.AbstractAuthMailPhoneForm;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.registration.model.AuthContext;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.phone.ActivationCodeType;
import ru.alamics.sso.registration.phone.UserPhoneVerifier;
import ru.alamics.sso.registration.phone.exception.PhoneCallException;
import ru.alamics.sso.registration.phone.exception.SendMessageException;
import ru.alamics.sso.registration.phone.exception.UserPhoneEmpty;
import ru.alamics.sso.registration.phone.exception.WrongSmsCode;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.Util;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.alamics.sso.registration.model.UserConstants.AUTH_FORM_SUCCESS;
import static ru.alamics.sso.registration.phone.ActivationCodeType.CODE_BY_PHONE_NUMBER;
import static ru.alamics.sso.registration.phone.ActivationCodeType.CODE_TO_SMS;
import static ru.alamics.sso.registration.phone.UserPhoneVerifier.*;
import static ru.alamics.sso.registration.phone.UserPhoneVerifier.COUNT_REPEAT;
import static ru.alamics.sso.settings.SettingConstants.*;
import static ru.alamics.sso.settings.SettingConstants.PHONE_CONST_LINK;


@Slf4j
public abstract class NewAbstractAuthMailPhoneForm extends AbstractAuthMailPhoneForm {

    private final UserFindService userFindService;

    private ActivationCodeType activationCodeType;

    private final UserPhoneVerifier userPhoneVerifier;

    private final SettingsService settingsService;

    private static final String ERROR_CODE = "error_code";

    public NewAbstractAuthMailPhoneForm(UserFindService userFindService) {
        super(userFindService);
        this.userFindService = userFindService;
        this.userPhoneVerifier = Lookup.lookup(UserPhoneVerifier.class);
        this.settingsService = Lookup.lookup(SettingsService.class);
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
                user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);
            } else {
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

    @Override
    public void action(AuthenticationFlowContext context) {
        HttpRequest httpRequest = context.getHttpRequest();
        MultivaluedMap<String, String> formData = httpRequest.getDecodedFormParameters();
        AuthenticationSessionModel sessionModel = context.getAuthenticationSession();

        context.form().setAttribute("isSwitcherOn", getCurrentSwitcherStatus(context.getHttpRequest(), context));

        context.getAuthenticationSession().removeAuthNote("backToLoginPassword");

        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }

        final boolean isLoginPassword = httpRequest.getDecodedFormParameters().containsKey("loginPasswordButton");
        final boolean isSms = httpRequest.getDecodedFormParameters().containsKey("smsButton");
        final boolean isPhoneCall = httpRequest.getDecodedFormParameters().containsKey("phoneCallButton");

        if (isLoginPassword && (validateUserAndPassword(context, formData))) {
            sessionModel.setAuthNote("loginPasswordButton", "loginPasswordButton");
            context.success();
            return;
        }

        if ((isSms || isPhoneCall) && (validateUserAndPassword(context, formData))) {
            sessionModel.setAuthNote("secondPhase", (!isSms ? "phoneCallButton" : "smsButton"));
            authenticate(context);
            return;
        }

        if (sessionModel.getAuthNote("secondPhase") != null) {
            UserModel model = context.getUser();
            User user = UserModelUserMapper.mapToUser(model);

            AuthContext authContext = AuthContext.builder()
                    .hashProperty(sessionModel.getAuthNote(PHONE_KEY_HASH))
                    .expirationTime(LocalDateTime.parse(sessionModel.getAuthNote(EXPIRATION_TIME), DateTimeFormatter.ISO_DATE_TIME))
                    .counter(getCount(sessionModel.getAuthNote(COUNT_REPEAT)))
                    .activationCodeType(activationCodeType)
                    .build();

            try {
                String code = httpRequest.getDecodedFormParameters().getFirst("smscode");

                userPhoneVerifier.verifyPhone(user, authContext, code, activationCodeType);

                sessionModel.removeAuthNote(PHONE_KEY_HASH);
                sessionModel.removeAuthNote(EXPIRATION_TIME);
                sessionModel.removeAuthNote(COUNT_REPEAT);

                sessionModel.setAuthNote(AUTH_FORM_SUCCESS, Util.TRUE_STR);
                sessionModel.setAuthNote(sessionModel.getAuthNote("secondPhase"), "");
                context.success();

            } catch (WrongSmsCode wrongSmsCode) {
                log.warn("Wrong sms code");
                context.form()
                        .setAttribute("error", "Пароль введен не верно. Вам выслан новый код")
                        .setError("Код введен неверно. Проверьте правильность введенных данных");
                sessionModel.removeAuthNote(PHONE_KEY_HASH);
                sessionModel.setAuthNote(ERROR_CODE, ERROR_CODE);
                authenticate(context);
            }
        }
    }


    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
        String loginHint = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);
        String rememberMeUsername = AuthenticationManager.getRememberMeUsername(context.getRealm(), context.getHttpRequest().getHttpHeaders());
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        context.form().setAttribute("isSwitcherOn", getCurrentSwitcherStatus(context.getHttpRequest(), context));

        if (context.getHttpRequest().getDecodedFormParameters().containsKey("back")) {
            authSession.setAuthNote("backToLoginPassword", "backToLoginPassword");
            authSession.removeAuthNote("secondPhase");
            context.form().setAttribute("isSwitcherOn", getCurrentSwitcherStatus(context.getHttpRequest(), context));
            context.challenge(challenge(context, formData));
            return;
        }

        if (loginHint != null) {
            formData.add(AuthenticationManager.FORM_USERNAME, loginHint);
        } else if (rememberMeUsername != null) {
            formData.add(AuthenticationManager.FORM_USERNAME, rememberMeUsername);
            formData.add("rememberMe", "on");
        }

        if (authSession.getAuthNote("secondPhase") != null) {
            activationCodeType = authSession.getAuthNote("secondPhase").equals("smsButton") ? CODE_TO_SMS : CODE_BY_PHONE_NUMBER;
            User user = UserModelUserMapper.mapToUser(context.getUser());

            AuthContext authContext = AuthContext.builder()
                    .activationCodeType(activationCodeType)
                    .expirationTime(LocalDateTime.now().plusSeconds(activationCodeType.getExpiredSeconds()))
                    .hashProperty(authSession.getAuthNote(PHONE_KEY_HASH))
                    .counter(getCount(authSession.getAuthNote(COUNT_REPEAT)))
                    .build();

            try {
                boolean enableRepeatCall = true;
                authContext = userPhoneVerifier.sendValidationMsg(user, authContext, activationCodeType, context.getRealm());

                authSession.setAuthNote(PHONE_KEY_HASH, authContext.getHashProperty());
                authSession.setAuthNote(EXPIRATION_TIME, authContext.getExpirationTime().format(DateTimeFormatter.ISO_DATE_TIME));
                authSession.setAuthNote(COUNT_REPEAT, authContext.getCounter().toString());

                LoginFormsProvider loginFormsProvider = context.form()
                        .setAttribute("userPhone", user.getPhone())
                        .setAttribute("expirationSeconds", String.valueOf(authContext.getActivationCodeType().getExpiredSeconds()))
                        .setAttribute("lengthCode", authContext.getActivationCodeType().getLengthCode())
                        .setAttribute("activationCodeType", authContext.getActivationCodeType().name())
                        .setAttribute("enableRepeatCall", enableRepeatCall)
                        .setAttribute("sendAgain", settingsService.getSettingsStringValue(SEND_AGAIN, context.getRealm().getId()))
                        .setAttribute("doSubmit", settingsService.getSettingsStringValue(DO_SUBMIT, context.getRealm().getId()))
                        .setAttribute("homePage", settingsService.getSettingsStringValue(HOME_PAGE, context.getRealm().getId()))
                        .setAttribute("phoneConst", settingsService.getSettingsStringValue(PHONE_CONST, context.getRealm().getId()))
                        .setAttribute("footer", settingsService.getSettingsStringValue(FOOTER, context.getRealm().getId()))
                        .setAttribute("phoneConstLink", settingsService.getSettingsStringValue(PHONE_CONST_LINK, context.getRealm().getId()))
                        .setAttribute("phoneCallButton", authSession.getAuthNote("secondPhase").equals("phoneCallButton"));


                context.challenge(challenge(loginFormsProvider));

            } catch (UserPhoneEmpty userPhoneEmpty) {
                log.info("ignore... userPhoneEmpty");
            } catch (PhoneCallException e) {
                log.info("ignore... PhoneCallException {}", e.getMessage());
            } catch (SendMessageException se) {
                log.info("ignore... MsgSendException {}", se.getMessage());
            }
        } else {
            context.challenge(challenge(context, formData));
        }
    }

    protected Response challenge(LoginFormsProvider loginFormsProvider) {
        return loginFormsProvider.createForm("sms-phone.ftl");
    }

    protected Boolean getCurrentSwitcherStatus(HttpRequest httpRequest, AuthenticationFlowContext context) {
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();

        final String loginPasswordAuthNote = "loginPassword";
        final String loginSmsAuthNote = "loginSms";
        final String loginPhoneCallAuthNote = "loginPhoneCall";

        boolean isOff = httpRequest.getDecodedFormParameters().containsKey("off");
        boolean isOn = httpRequest.getDecodedFormParameters().containsKey("on");

        boolean isLoginPassword = Boolean.parseBoolean(context.getAuthenticationSession().getClient().getAttribute("loginViaEmailOrUsernameAndPassword"));
        boolean isSms = Boolean.parseBoolean(context.getAuthenticationSession().getClient().getAttribute("loginViaSms"));
        boolean isPhoneCall = Boolean.parseBoolean(context.getAuthenticationSession().getClient().getAttribute("loginViaPhoneCall"));

        if (authenticationSession.getAuthNote("backToLoginPassword") != null) {
            if (isLoginPassword) {
                authenticationSession.removeAuthNote(loginSmsAuthNote);
                authenticationSession.removeAuthNote("backToLoginPassword");
            }
            if (isSms) {
                authenticationSession.setAuthNote(loginPasswordAuthNote, loginPasswordAuthNote);
                authenticationSession.removeAuthNote("backToLoginPassword");
            }
            if (isPhoneCall) {
                authenticationSession.setAuthNote(loginPasswordAuthNote, loginPasswordAuthNote);
                authenticationSession.removeAuthNote("backToLoginPassword");
            }
        }

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

    private Integer getCount(String countStr) {
        if (countStr == null || "null".equals(countStr)) {
            return 0;
        } else {
            return Integer.valueOf(countStr);
        }
    }
}



