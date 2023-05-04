package ru.alamics.sso.keycloak.auth.form.new_auth;


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
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.MediaType;
import ru.alamics.sso.antifraud.AttemptFailsDto;
import ru.alamics.sso.antifraud.AttemptFailsService;
import ru.alamics.sso.antifraud.BlackListDto;
import ru.alamics.sso.antifraud.BlackListService;
import ru.alamics.sso.jpa.util.LimitationCauseType;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.keycloak.util.VerifyPhoneKey;
import ru.alamics.sso.registration.model.AuthContext;
import ru.alamics.sso.registration.model.MessageConstants;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.phone.ActivationCodeType;
import ru.alamics.sso.registration.phone.UserPhoneVerifier;
import ru.alamics.sso.registration.phone.exception.*;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.Util;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static ru.alamics.sso.registration.model.UserConstants.AUTH_FORM_SUCCESS;
import static ru.alamics.sso.registration.phone.ActivationCodeType.CODE_BY_PHONE_NUMBER;
import static ru.alamics.sso.registration.phone.ActivationCodeType.CODE_TO_SMS;
import static ru.alamics.sso.registration.phone.UserPhoneVerifier.*;
import static ru.alamics.sso.settings.SettingConstants.*;


@Slf4j
public abstract class NewAbstractAuthMailPhoneForm extends AbstractUsernameFormAuthenticator implements Authenticator {

    private final UserFindService userFindService;

    private ActivationCodeType activationCodeType;

    private final UserPhoneVerifier userPhoneVerifier;

    private final SettingsService settingsService;

    private static final String ERROR_CODE = "error_code";

    private final BlackListService blackListService;

    private static final String GRANT_TYPE = "grant_type";

    private static final ConcurrentHashMap<PhonePlusRealmProtector, VerifyPhoneKey> mainCounter = new ConcurrentHashMap<>();

    private final AttemptFailsService attemptFailsService;

    private static final int MAX_RESEND_RECALL_TRIES = 5;

    private static final ConcurrentHashMap<PhonePlusRealmProtector, PhoneHashAndBanStatusKeeper> currentAuthFlowPhoneNumbers = new ConcurrentHashMap<>();

    private static final int COUNT_BY_ONE_CODE = 5;

    public NewAbstractAuthMailPhoneForm(UserFindService userFindService) {
        this.userFindService = userFindService;
        this.userPhoneVerifier = Lookup.lookup(UserPhoneVerifier.class);
        this.settingsService = Lookup.lookup(SettingsService.class);
        this.blackListService = Lookup.lookup(BlackListService.class);
        this.attemptFailsService = Lookup.lookup(AttemptFailsService.class);
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
        String loginHint = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);
        String rememberMeUsername = AuthenticationManager.getRememberMeUsername(context.getRealm(), context.getHttpRequest().getHttpHeaders());
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        if (context.getHttpRequest().getDecodedFormParameters().containsKey("back")) {
            authSession.setAuthNote("backToLoginPassword", "backToLoginPassword");
            authSession.removeAuthNote("secondPhase");
            context.form().setAttribute("isSwitcherOn", getCurrentSwitcherStatus(context.getHttpRequest(), context));
            authSession.removeAuthNote("needSendSmsCode");
            authSession.removeAuthNote(PHONE_KEY_HASH);
            context.clearUser();
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

            PhonePlusRealmProtector protector = new PhonePlusRealmProtector(user.getPhone(), context.getRealm());

            if (!currentAuthFlowPhoneNumbers.containsKey(protector)) {
                currentAuthFlowPhoneNumbers.put(protector, new PhoneHashAndBanStatusKeeper(true, ""));
            }

            long deltaTime = 0;

            AuthContext authContext = AuthContext.builder()
                    .activationCodeType(activationCodeType)
                    .expirationTime(LocalDateTime.now().plusSeconds(activationCodeType.getExpiredSeconds()))
                    .hashProperty(authSession.getAuthNote(PHONE_KEY_HASH))
                    .counter(getCount(authSession.getAuthNote(COUNT_REPEAT)))
                    .build();

            BlackListDto blackListDto = blackListService.getBlockedUser(user.getPhone(), context);
            if (Objects.isNull(authSession.getAuthNote(EXPIRATION_TIME))) {
                authSession.setAuthNote(EXPIRATION_TIME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                authSession.setAuthNote("correctTime", LocalDateTime.now().plusSeconds(activationCodeType.getExpiredSeconds()).format(DateTimeFormatter.ISO_DATE_TIME));
            }
            setTimerValueByActivationType(blackListDto, user, authSession, authContext, deltaTime, context);
            try {
                boolean enableRepeatCall = true;
                if (authSession.getAuthNote("needSendSmsCode") != null && authSession.getAuthNote("needSendSmsCode").equals("true")) {
                    authContext = userPhoneVerifier.sendValidationMsg(user, authContext, activationCodeType, context.getRealm());
                    authSession.setAuthNote("godMode", authContext.getHashProperty());
                    authSession.setAuthNote("needSendSmsCode", "false");
                    authSession.setAuthNote("currentCode", authContext.getHashProperty());
                    currentAuthFlowPhoneNumbers.get(protector).setSavedCodeHash(authContext.getHashProperty());
                } else if (checkCanWeSendSmS(user, context, protector) && authSession.getAuthNote("needSendSmsCode") != null && authSession.getAuthNote("needSendSmsCode").equals("true")
                        && isPhoneAbuseActivated(protector) || checkCanWeSendSmS(user, context, protector) && authSession.getAuthNote("needSendSmsCode") == null && isPhoneAbuseActivated(protector)) {
                    authContext = userPhoneVerifier.sendValidationMsg(user, authContext, activationCodeType, context.getRealm());
                    authSession.setAuthNote("godMode", authContext.getHashProperty());
                    authSession.setAuthNote("needSendSmsCode", "false");
                    currentAuthFlowPhoneNumbers.get(protector).setSendingBanned(false);
                    currentAuthFlowPhoneNumbers.get(protector).setSavedCodeHash(authContext.getHashProperty());
                }
                authSession.setAuthNote(PHONE_KEY_HASH, authContext.getHashProperty());
                authSession.setAuthNote(COUNT_REPEAT, authContext.getCounter().toString());

                LoginFormsProvider loginFormsProvider = context.form()
                        .setAttribute("userPhone", user.getPhone())
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


                context.challenge(challenge(context, loginFormsProvider));

            } catch (UserPhoneEmpty userPhoneEmpty) {
                log.info("ignore... userPhoneEmpty");
            } catch (PhoneCallException e) {
                log.info("ignore... PhoneCallException {}", e.getMessage());
                context.form()
                        .setError("Авторизация с использованием временного кода в данный момент не доступна. Для авторизации воспользуйтесь логином и паролем");
                context.resetFlow();
            } catch (SendMessageException se) {
                log.info("ignore... MsgSendException {}", se.getMessage());
                context.form()
                        .setError("Авторизация с использованием временного кода в данный момент не доступна. Для авторизации воспользуйтесь логином и паролем");
                context.resetFlow();
            }
        } else {
            context.challenge(challenge(context, formData));
        }
    }

    private boolean isPhoneAbuseActivated(PhonePlusRealmProtector protector) {
        return currentAuthFlowPhoneNumbers.get(protector).isSendingBanned();
    }

    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = context.form();
        forms.setAttribute("isSwitcherOn", getCurrentSwitcherStatus(context.getHttpRequest(), context));

        if (formData.size() > 0) forms.setFormData(formData);

        return createLoginForm(forms);
    }

    protected Response challenge(AuthenticationFlowContext context, LoginFormsProvider provider) {
        String mp = context.getAuthenticationSession().getAuthNote("MP");
        String errorCode = context.getAuthenticationSession().getAuthNote(ERROR_CODE);
        provider.setAttribute("isSwitcherOn", getCurrentSwitcherStatus(context.getHttpRequest(), context));

        if (mp != null && errorCode != null) {
            Response response = provider.createForm("sms-phone.ftl");
            Map<String, String> entity = (Map<String, String>) response.getEntity();
            entity.put("error", "Код введен неверно. Вам выслан новый код");
            return Response.ok().entity(entity).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        if (mp != null) {
            HttpRequest contextObject = context.getSession().getContext().getContextObject(HttpRequest.class);
            MultivaluedMap<String, String> parameters = contextObject.getDecodedFormParameters();
            parameters.add(GRANT_TYPE, "password");
        }

        return provider.createForm("sms-phone.ftl");
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        HttpRequest httpRequest = context.getHttpRequest();
        MultivaluedMap<String, String> formData = httpRequest.getDecodedFormParameters();
        AuthenticationSessionModel sessionModel = context.getAuthenticationSession();
        sessionModel.removeAuthNote("backToLoginPassword");

        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }

        final boolean isLoginPassword = httpRequest.getDecodedFormParameters().containsKey("loginPasswordButton");
        final boolean isSms = httpRequest.getDecodedFormParameters().containsKey("smsButton");
        final boolean isPhoneCall = httpRequest.getDecodedFormParameters().containsKey("phoneCallButton");

        if (isLoginPassword && (validateUserAndPassword(context, formData))) {
            sessionModel.setAuthNote("loginPasswordButton", "loginPasswordButton");
            sessionModel.setAuthNote(AUTH_FORM_SUCCESS, Util.TRUE_STR);
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
            PhonePlusRealmProtector protector = new PhonePlusRealmProtector(user.getPhone(), context.getRealm());

            activationCodeType = sessionModel.getAuthNote("secondPhase").equals("smsButton") ? CODE_TO_SMS : CODE_BY_PHONE_NUMBER;
            AuthContext authContext = AuthContext.builder()
                    .hashProperty(currentAuthFlowPhoneNumbers.get(protector) != null ? currentAuthFlowPhoneNumbers.get(protector).getSavedCodeHash() : "")
                    .expirationTime(LocalDateTime.parse(sessionModel.getAuthNote("correctTime"), DateTimeFormatter.ISO_DATE_TIME))
                    .counter(getCount(sessionModel.getAuthNote(COUNT_REPEAT)))
                    .activationCodeType(activationCodeType)
                    .build();
            if (Objects.nonNull(authContext.getHashProperty()) || !authContext.getHashProperty().equals("")) {
                sessionModel.setAuthNote("currentCode", authContext.getHashProperty());
            }
            if (context.getHttpRequest().getDecodedFormParameters().containsKey("resend")) {
                String currentCode = sessionModel.getAuthNote("currentCode");

                if (activationCodeType.equals(CODE_BY_PHONE_NUMBER)) {
                    attemptFailsService.saveAttempt(new AttemptFailsDto(user.getPhone(), currentCode, context.getRealm().getName(), CODE_BY_PHONE_NUMBER.name()));
                }

                if (activationCodeType.equals(CODE_TO_SMS)) {
                    attemptFailsService.saveAttempt(new AttemptFailsDto(user.getPhone(), currentCode, context.getRealm().getName(), CODE_TO_SMS.name()));
                }

                if (mainCounter.get(protector).getCurrentCode().equals(currentCode)) {
                    mainCounter.remove(protector);
                }
                checkIsLimited(user, context, sessionModel, protector);
                log.info("Sms code resend");

                sessionModel.setAuthNote("needSendSmsCode", "true");
                sessionModel.setAuthNote(EXPIRATION_TIME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                sessionModel.removeAuthNote(PHONE_KEY_HASH);
                authenticate(context);

            } else {
                checkAndAddToCounter(user, authContext, context, sessionModel, protector);
                verifyCode(context, sessionModel, user, authContext, httpRequest, protector);
            }
        } else {
            authenticate(context);
        }
    }

    public void checkIsLimited(User user, AuthenticationFlowContext context, AuthenticationSessionModel authSession, PhonePlusRealmProtector protector) {
        if (activationCodeType.equals(CODE_TO_SMS)) {
            List<AttemptFailsDto> smsAttempts = attemptFailsService.getAttempts(user.getPhone(), context.getRealm().getName(), CODE_TO_SMS.name());
            if (!smsAttempts.isEmpty() && smsAttempts.size() >= MAX_RESEND_RECALL_TRIES && !blackListService.isUserBlockedAuthBySms(user.getPhone(), context)) {
                blackListService.limitUserBySmsOrPhone(user, LimitationCauseType.SMS, authSession);
                authenticate(context);
                mainCounter.remove(protector);
                attemptFailsService.deleteAttempts(smsAttempts);
                return;
            }
        }

        if (activationCodeType.equals(CODE_BY_PHONE_NUMBER)) {
            List<AttemptFailsDto> callAttempts = attemptFailsService.getAttempts(user.getPhone(), context.getRealm().getName(), CODE_BY_PHONE_NUMBER.name());
            if (!callAttempts.isEmpty() && callAttempts.size() >= MAX_RESEND_RECALL_TRIES && !blackListService.isUserBlockedAuthByPhoneCall(user.getPhone(), context)) {
                blackListService.limitUserBySmsOrPhone(user, LimitationCauseType.PHONE_CALL, authSession);
                authenticate(context);
                mainCounter.remove(protector);
                attemptFailsService.deleteAttempts(callAttempts);
            }
        }
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
                    context.form().setAttribute("error", "Введён неверный E-mail");
                    authenticate(context);
                    return false;
                }
                break;
            case "phone":
                if (!(username.matches(phoneRegex))) {
                    context.form().setAttribute("error", "Введён неверный номер телефона");
                    authenticate(context);
                    return false;
                }
        }
        return true;
    }

    private Integer getCount(String countStr) {
        if (countStr == null || "null".equals(countStr)) {
            return 0;
        } else {
            return Integer.valueOf(countStr);
        }
    }

    private void verifyCode(AuthenticationFlowContext context, AuthenticationSessionModel sessionModel, User user, AuthContext authContext, HttpRequest httpRequest, PhonePlusRealmProtector protector) {
        try {
            String code = httpRequest.getDecodedFormParameters().getFirst("smscode");

            userPhoneVerifier.verifyPhone(user, authContext.getExpirationTime(), authContext.getHashProperty(), code, activationCodeType);

            sessionModel.removeAuthNote(PHONE_KEY_HASH);
            sessionModel.removeAuthNote(EXPIRATION_TIME);
            sessionModel.removeAuthNote(COUNT_REPEAT);

            sessionModel.setAuthNote(AUTH_FORM_SUCCESS, Util.TRUE_STR);
            sessionModel.setAuthNote(sessionModel.getAuthNote("secondPhase"), "");
            context.success();
            sessionModel.removeAuthNote("needSendSmsCode");
            currentAuthFlowPhoneNumbers.remove(protector);
        } catch (WrongSmsCode wrongSmsCode) {
            log.warn("Wrong sms code");
            context.form()
                    .setError("Код введен неверно. Проверьте правильность введенных данных");
            sessionModel.removeAuthNote(PHONE_KEY_HASH);
            sessionModel.setAuthNote(ERROR_CODE, ERROR_CODE);
            authenticate(context);
        } catch (TimeExpiredException e) {
            log.warn("Time for code is expired");
            context.form()
                    .setError("Истёк срок действия кода");
            sessionModel.removeAuthNote(PHONE_KEY_HASH);
            sessionModel.setAuthNote(ERROR_CODE, ERROR_CODE);
            authenticate(context);
        }
    }

    private void checkAndIncrementExistTries(AuthContext authContext, AuthenticationFlowContext context, PhonePlusRealmProtector protector) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        String code = authSession.getAuthNote("currentCode");
        if (Objects.isNull(code)) {
            return;
        }
        if (mainCounter.containsKey(protector)) {
            int existCurrentCodeTries = 0;
            VerifyPhoneKey verifyPhoneKey = mainCounter.get(protector);
            if (verifyPhoneKey.getCurrentCode().equals(code) && verifyPhoneKey.getRealm().equals(authSession.getRealm().getName())) {
                existCurrentCodeTries = verifyPhoneKey.getCurrentCodeCounter();
            }
            existCurrentCodeTries++;

            verifyPhoneKey = new VerifyPhoneKey(authContext.getActivationCodeType(), code, existCurrentCodeTries, authSession.getRealm().getName());
            mainCounter.put(protector, verifyPhoneKey);
        }
    }

    private boolean checkIsMoreThanFiveAttempts(AuthenticationFlowContext context, PhonePlusRealmProtector protector) {
        VerifyPhoneKey currentCodeCounter = mainCounter.get(protector);
        if (currentCodeCounter == null) {
            context.form().setAttribute("isMoreThanFiveAttempts", false);
            return false;
        }
        String code = context.getAuthenticationSession().getAuthNote("currentCode");
        if (currentCodeCounter
                .getCurrentCode().equals(code) && currentCodeCounter
                .getCurrentCodeCounter() >= COUNT_BY_ONE_CODE && currentCodeCounter.getActivationType().equals(CODE_TO_SMS)
                && currentCodeCounter.getRealm().equals(context.getRealm().getName())) {
            context.form().setAttribute("isMoreThanFiveAttempts", true)
                    .setError(MessageConstants.SMS_LIMIT_5_CONTINUE);
            return true;
        }

        if (currentCodeCounter
                .getCurrentCode().equals(code) && currentCodeCounter
                .getCurrentCodeCounter() >= COUNT_BY_ONE_CODE && currentCodeCounter.getActivationType().equals(CODE_BY_PHONE_NUMBER)
                && currentCodeCounter.getRealm().equals(context.getRealm().getName())) {
            context.form().setAttribute("isMoreThanFiveAttempts", true)
                    .setError(MessageConstants.CALL_LIMIT_5_CONTINUE);
            return true;
        }
        context.form().setAttribute("isMoreThanFiveAttempts", false);
        return false;
    }

    private void checkAndAddToCounter(User user, AuthContext authContext, AuthenticationFlowContext context, AuthenticationSessionModel sessionModel, PhonePlusRealmProtector protector) {
        VerifyPhoneKey verifyPhoneKey = mainCounter.get(protector);
        if (!mainCounter.containsKey(protector) || verifyPhoneKey
                .getCurrentCodeCounter() == null || !verifyPhoneKey.getRealm().equals(context.getRealm().getName())) {
            VerifyPhoneKey initCounter = new VerifyPhoneKey(authContext.getActivationCodeType(),
                    sessionModel.getAuthNote("currentCode"), 0, context.getRealm().getName()); /* 0 - кол-во попыток изначально */
            mainCounter.put(protector, initCounter);
        }

        checkAndIncrementExistTries(authContext, context, protector);
    }

    private boolean checkCanWeSendSmS(User user, AuthenticationFlowContext context, PhonePlusRealmProtector protector) {
        if (activationCodeType.equals(CODE_TO_SMS) && blackListService.isUserBlockedAuthBySms(user.getPhone(), context)) {
            context.form()
                    .setAttribute("codeLimited", true)
                    .setError(MessageConstants.SMS_LIMIT_BLOCK);
            currentAuthFlowPhoneNumbers.remove(protector);
            return false;
        }
        if (activationCodeType.equals(CODE_BY_PHONE_NUMBER) && blackListService.isUserBlockedAuthByPhoneCall(user.getPhone(), context)) {
            context.form()
                    .setAttribute("codeLimited", true)
                    .setError(MessageConstants.CALL_LIMIT_BLOCK);
            currentAuthFlowPhoneNumbers.remove(protector);
            return false;
        }
        return checkIsMoreThanFiveAttempts(context, protector) || !mainCounter.containsKey(protector);
    }

    private void setTimerValueByActivationType(BlackListDto blackListDto, User user, AuthenticationSessionModel authSession, AuthContext authContext, long deltaTime, AuthenticationFlowContext context) {
        if (Objects.nonNull(blackListDto)) {
            if (blackListService.isUserBlockedAuthBySms(user.getPhone(), context) && activationCodeType.equals(CODE_TO_SMS)) {
                setBlockedTime(blackListDto, authSession, deltaTime, context);
                return;
            }
            if (blackListService.isUserBlockedAuthByPhoneCall(user.getPhone(), context) && activationCodeType.equals(CODE_BY_PHONE_NUMBER)) {
                setBlockedTime(blackListDto, authSession, deltaTime, context);
                return;
            }
        }
        LocalDateTime previousTime = LocalDateTime.parse(authSession.getAuthNote(EXPIRATION_TIME), DateTimeFormatter.ISO_DATE_TIME);
        deltaTime = Duration.between(previousTime, LocalDateTime.now()).getSeconds();
        context.form().setAttribute("expirationSeconds", String.valueOf(authContext.getActivationCodeType().getExpiredSeconds() - deltaTime));
    }

    private void setBlockedTime(BlackListDto blackListDto, AuthenticationSessionModel authSession, Long deltaTime, AuthenticationFlowContext context) {
        LocalDateTime unblocked = blackListDto.getUnblockedAt();
        authSession.setAuthNote(EXPIRATION_TIME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        LocalDateTime previousTime = LocalDateTime.parse(authSession.getAuthNote(EXPIRATION_TIME), DateTimeFormatter.ISO_DATE_TIME);
        deltaTime = previousTime.until(unblocked, ChronoUnit.SECONDS);
        context.form().setAttribute("expirationSeconds", String.valueOf(deltaTime))
                .setAttribute("codeLimited", true);
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

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    public abstract boolean isSuccessCheckUser(AuthenticationFlowContext context, UserModel user);
}
