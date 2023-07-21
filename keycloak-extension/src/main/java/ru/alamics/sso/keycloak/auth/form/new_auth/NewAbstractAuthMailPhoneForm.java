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
import ru.alamics.sso.antifraud.*;
import ru.alamics.sso.auth_n_regi.AuthOrRegTypeNotFoundException;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.keycloak.util.UserToUserEntityMapper;
import ru.alamics.sso.registration.model.AuthContext;
import ru.alamics.sso.registration.model.MessageConstants;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.phone.ActivationCodeType;
import ru.alamics.sso.registration.phone.HashGenerator;
import ru.alamics.sso.registration.phone.SmsCodeGenerator;
import ru.alamics.sso.registration.phone.UserPhoneVerifier;
import ru.alamics.sso.registration.phone.exception.*;
import ru.alamics.sso.registration.phone.port.PhoneCallerRemoteService;
import ru.alamics.sso.registration.phone.port.SendMessageService;
import ru.alamics.sso.registration.rias.RiasService;
import ru.alamics.sso.registration.service.AuthOrRegTypeService;
import ru.alamics.sso.registration.service.AuthorisedUsersService;
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

import static ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil.addRequiredAction;
import static ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil.getAuthOrRegType;
import static ru.alamics.sso.keycloak.auth.form.new_auth.newAuthReqActions.TwoStepAuthFactory.CLIENT_B2B;
import static ru.alamics.sso.registration.model.UserConstants.AUTH_FORM_SUCCESS;
import static ru.alamics.sso.registration.phone.ActivationCodeType.*;
import static ru.alamics.sso.registration.phone.UserPhoneVerifier.*;
import static ru.alamics.sso.settings.SettingConstants.*;


@Slf4j
public abstract class NewAbstractAuthMailPhoneForm extends AbstractUsernameFormAuthenticator implements Authenticator {

    private final UserFindService userFindService;

    private ActivationCodeType activationCodeType;

    private final UserPhoneVerifier userPhoneVerifier;

    private final SettingsService settingsService;

    private final WroteCodeAttemptsService wroteCodeAttemptsService;

    private final AuthorisedUsersService authorisedUsersService;
    private final SendMessageService messageSendService;

    private final PhoneCallerRemoteService phoneCallerService;

    private static final String ERROR_CODE = "error_code";

    private final BlackListService blackListService;

    private static final String GRANT_TYPE = "grant_type";

    private static final String CODE_HASH_KEY = "CODE_HASH_KEY";

    private final AttemptFailsService attemptFailsService;

    private static final int MAX_RESEND_RECALL_TRIES = 4; // на самом деле 5

    private static final ConcurrentHashMap<PhonePlusRealmProtector, PhoneHashAndBanStatusKeeper> currentAuthFlowPhoneNumbers = new ConcurrentHashMap<>();

    private static final int COUNT_BY_ONE_CODE = 5;

    private final RiasService riasService;


    public NewAbstractAuthMailPhoneForm(UserFindService userFindService) {
        this.userFindService = userFindService;
        this.userPhoneVerifier = Lookup.lookup(UserPhoneVerifier.class);
        this.settingsService = Lookup.lookup(SettingsService.class);
        this.blackListService = Lookup.lookup(BlackListService.class);
        this.attemptFailsService = Lookup.lookup(AttemptFailsService.class);
        this.wroteCodeAttemptsService = Lookup.lookup(WroteCodeAttemptsService.class);
        this.messageSendService = Lookup.lookup(SendMessageService.class, "MessageSender");
        this.phoneCallerService = Lookup.lookup(PhoneCallerRemoteService.class, "PhoneCallerService");
        this.riasService = Lookup.lookup(RiasService.class);
        this.authorisedUsersService = Lookup.lookup(AuthorisedUsersService.class);
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
            authSession.removeAuthNote(CODE_HASH_KEY);
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
                    .expirationTime(LocalDateTime.now().plusSeconds(activationCodeType.getExpiredSecondsToResend()))
                    .hashProperty(authSession.getAuthNote(CODE_HASH_KEY))
                    .counter(getCount(authSession.getAuthNote(COUNT_REPEAT)))
                    .build();

            BlackListDto blackListDto = blackListService.getBlockedUser(user.getPhone(), context);
            if (Objects.isNull(authSession.getAuthNote(EXPIRATION_TIME))) {
                authSession.setAuthNote(EXPIRATION_TIME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                authSession.setAuthNote("correctTime", LocalDateTime.now().plusSeconds(activationCodeType.getExpiredSecondsToResend()).format(DateTimeFormatter.ISO_DATE_TIME));
            }
            setTimerValueByActivationType(blackListDto, user, authSession, authContext, deltaTime, context);
            try {
                if (user.getPhone() == null || user.getPhone().isEmpty())
                    throw new UserPhoneEmpty();
                if (Objects.isNull(authSession.getAuthNote("currentCode")) || authSession.getAuthNote("currentCode").equals("")) {
                    authSession.setAuthNote("needSendSmsCode", "true");
                }
                boolean enableRepeatCall = true;

                if (sendIfNotBan(user, context, protector) && authSession.getAuthNote("needSendSmsCode") != null && authSession.getAuthNote("needSendSmsCode").equals("true")) {
                    switch (activationCodeType) {
                        case CODE_TO_SMS: {
                            String code = SmsCodeGenerator.getCode(activationCodeType.getLengthCode());
                            authSession.setAuthNote(CODE_HASH_KEY, HashGenerator.getSecretHash(code));

                            String[] messengerList = context.getRealm().getSmtpConfig().get(MESSENGER).split(",");
                            messageSendService.sendMessageToMessengers(user.getPhone(), code, context.getRealm().getId(), messengerList);
                            break;
                        }
                        case CODE_BY_PHONE_NUMBER: {
                            String code = phoneCallerService.callAndGetCode(user.getPhone(), 1);
                            authSession.setAuthNote(CODE_HASH_KEY, HashGenerator.getSecretHash(code));
                            break;
                        }
                    }
                    authSession.setAuthNote("godMode", authSession.getAuthNote(CODE_HASH_KEY));
                    authSession.setAuthNote("needSendSmsCode", "false");
                    authSession.setAuthNote("currentCode", authSession.getAuthNote(CODE_HASH_KEY));
                    currentAuthFlowPhoneNumbers.get(protector).setSavedCodeHash(authSession.getAuthNote(CODE_HASH_KEY));
                } else if (checkCanWeSendSmS(user, context, protector) && authSession.getAuthNote("needSendSmsCode") != null && authSession.getAuthNote("needSendSmsCode").equals("true")
                        && isPhoneAbuseActivated(protector) || checkCanWeSendSmS(user, context, protector) && authSession.getAuthNote("needSendSmsCode") == null && isPhoneAbuseActivated(protector)) {
                    switch (activationCodeType) {
                        case CODE_TO_SMS: {
                            String code = SmsCodeGenerator.getCode(activationCodeType.getLengthCode());
                            authSession.setAuthNote(CODE_HASH_KEY, HashGenerator.getSecretHash(code));

                            String[] messengerList = context.getRealm().getSmtpConfig().get(MESSENGER).split(",");
                            messageSendService.sendMessageToMessengers(user.getPhone(), code, context.getRealm().getId(), messengerList);
                            break;
                        }
                        case CODE_BY_PHONE_NUMBER: {
                            String code = phoneCallerService.callAndGetCode(user.getPhone(), 1);
                            authSession.setAuthNote(CODE_HASH_KEY, HashGenerator.getSecretHash(code));
                            break;
                        }
                    }
                    authSession.setAuthNote("godMode", authSession.getAuthNote(CODE_HASH_KEY));
                    authSession.setAuthNote("needSendSmsCode", "false");
                    authSession.setAuthNote("currentCode", authSession.getAuthNote(CODE_HASH_KEY));
                    currentAuthFlowPhoneNumbers.get(protector).setSendingBanned(false);
                    currentAuthFlowPhoneNumbers.get(protector).setSavedCodeHash(authSession.getAuthNote(CODE_HASH_KEY));
                }
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
        ActivationCodeType.init(context.getRealm().getName());

        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }

        final boolean isLoginPassword = httpRequest.getDecodedFormParameters().containsKey("loginPasswordButton");
        final boolean isSms = httpRequest.getDecodedFormParameters().containsKey("smsButton");
        final boolean isPhoneCall = httpRequest.getDecodedFormParameters().containsKey("phoneCallButton");

        if (isLoginPassword && !isSuccessCheckUser(context, null)) {
            return;
        }

        if (isLoginPassword && (validateUserAndPassword(context, formData))) {
            doAuthActionForLogNPass(sessionModel, context);
            return;
        }

        if ((isSms || isPhoneCall) && (validateUserAndPassword(context, formData))) {
            sessionModel.setAuthNote("secondPhase", (!isSms ? "phoneCallButton" : "smsButton"));
            addEmptyReqForB2b(context, context.getUser());
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
            String code = sessionModel.getAuthNote("currentCode");
            /*if (Objects.nonNull(sessionModel.getAuthNote("currentCode")) && !sessionModel.getAuthNote("currentCode").equals("")) {
                sessionModel.setAuthNote("currentCode", sessionModel.getAuthNote(CODE_HASH_KEY));
            }*/
            if (checkIsLimited(user, context, sessionModel, protector, code)) {
                sessionModel.setAuthNote("needSendSmsCode", "false");
                sessionModel.removeAuthNote(EXPIRATION_TIME);
                sessionModel.removeAuthNote(CODE_HASH_KEY);
                authenticate(context);
                return;
            }
            if (context.getHttpRequest().getDecodedFormParameters().containsKey("resend")) {
                String currentCode = sessionModel.getAuthNote("currentCode");
                /*String codeHash = sessionModel.getAuthNote(CODE_HASH_KEY);*/
                if (currentCode != null && !currentCode.equals("")) {
                    if (activationCodeType.equals(CODE_BY_PHONE_NUMBER)) {
                        attemptFailsService.saveAttempt(new AttemptFailsDto(user.getPhone(), currentCode, context.getRealm().getName(), CODE_BY_PHONE_NUMBER.name(), LocalDateTime.now(), user.getId()));
                    }

                    if (activationCodeType.equals(CODE_TO_SMS)) {
                        attemptFailsService.saveAttempt(new AttemptFailsDto(user.getPhone(), currentCode, context.getRealm().getName(), CODE_TO_SMS.name(), LocalDateTime.now(), user.getId()));
                    }
                }

                if (checkIsLimited(user, context, sessionModel, protector, currentCode)) {
                    sessionModel.setAuthNote("needSendSmsCode", "false");
                    sessionModel.removeAuthNote(EXPIRATION_TIME);
                    sessionModel.removeAuthNote(CODE_HASH_KEY);
                    authenticate(context);
                    return;
                }
                log.info("Sms code resend");
                sessionModel.setAuthNote("needSendSmsCode", "true");
                sessionModel.removeAuthNote(EXPIRATION_TIME);

                authenticate(context);

            } else {
                verifyCode(context, sessionModel, user, authContext, httpRequest, protector);
            }
        } else {
            authenticate(context);
        }
    }

    public boolean checkIsLimited(User user, AuthenticationFlowContext context, AuthenticationSessionModel authSession, PhonePlusRealmProtector protector, String code) {
        if (activationCodeType.equals(CODE_TO_SMS)) {
            List<AttemptFailsDto> smsAttempts = attemptFailsService.getAttempts(user.getPhone(), context.getRealm().getName(), CODE_TO_SMS.name(), UserToUserEntityMapper.toUserEntity(user));
            if (!smsAttempts.isEmpty() && smsAttempts.size() > MAX_RESEND_RECALL_TRIES && !blackListService.isUserBlockedAuthBySms(user.getPhone(), context)) {
                if (wroteCodeAttemptsService.getWroteCodeAttemptsByCode(protector.getPhoneNumber(), protector.getUserRealm().getName(), CODE_TO_SMS.name(), code) >= 5) {
                    blackListService.limitUserBySmsOrPhone(user, activationCodeType.name(), authSession);
                    authenticate(context);
                    return true;
                }
            }
        }

        if (activationCodeType.equals(CODE_BY_PHONE_NUMBER)) {
            List<AttemptFailsDto> callAttempts = attemptFailsService.getAttempts(user.getPhone(), context.getRealm().getName(), CODE_BY_PHONE_NUMBER.name(), UserToUserEntityMapper.toUserEntity(user));
            if (!callAttempts.isEmpty() && callAttempts.size() >= MAX_RESEND_RECALL_TRIES && !blackListService.isUserBlockedAuthByPhoneCall(user.getPhone(), context)) {
                if (wroteCodeAttemptsService.getWroteCodeAttemptsByCode(protector.getPhoneNumber(), protector.getUserRealm().getName(), CODE_BY_PHONE_NUMBER.name(), code) >= 5) {
                    blackListService.limitUserBySmsOrPhone(user, activationCodeType.name(), authSession);
                    authenticate(context);
                    return true;
                }
            }
        }
        return false;
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
//            if (!isSuccessCheckUser(context, user)) {
//                return false;
//            }

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

        User commonUser = User.builder()
                .email("")
                .phone(username)
                .build();

        boolean isSmsOrPhone = inputData.containsKey("phoneCallButton") || inputData.containsKey("smsButton");

        if (isSmsOrPhone && riasService.checkPhone(commonUser)) {
            dummyHash(context);
            context.getEvent().error(Errors.USER_NOT_FOUND);
            Response challengeResponse = challenge(context, "Данный способ авторизации недоступен, воспользуйтесь входом через логин и пароль");
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);

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
        String codeHash = sessionModel.getAuthNote("currentCode");
        String code = httpRequest.getDecodedFormParameters().getFirst("smscode");
        try {

            userPhoneVerifier.verifyPhone(user, /*todo change expiration time to expire code +*/ activationCodeType.getExpiredCodeSeconds(),
                    codeHash, code, activationCodeType, sessionModel.getRealm().getName(), sessionModel);

            currentAuthFlowPhoneNumbers.remove(protector);
            doAuthActionForPhone(sessionModel, context);
        } catch (WrongSmsCode wrongSmsCode) {
            log.warn("Wrong sms code");
            context.form()
                    .setError("Код введен неверно. Проверьте правильность введенных данных");
            sessionModel.removeAuthNote(CODE_HASH_KEY);
            sessionModel.setAuthNote(ERROR_CODE, ERROR_CODE);
            wroteCodeAttemptsService.saveFailWroteCode(codeHash, code, protector.getPhoneNumber(), protector.getUserRealm().getName(), activationCodeType.name(), user);
            authenticate(context);
        } catch (TimeExpiredException e) {
            log.warn("Time for code is expired");
            context.form()
                    .setError("Истёк срок действия кода");
            sessionModel.removeAuthNote(CODE_HASH_KEY);
            sessionModel.setAuthNote(ERROR_CODE, ERROR_CODE);
            authenticate(context);
        }
    }

    private boolean checkIsMoreThanFiveAttempts(AuthenticationFlowContext context, PhonePlusRealmProtector protector) {

        String codeHash = context.getAuthenticationSession().getAuthNote("currentCode");
        if (wroteCodeAttemptsService.getWroteCodeAttemptsByCode(protector.getPhoneNumber(), protector.getUserRealm().getName(), CODE_TO_SMS.name(), codeHash) >= COUNT_BY_ONE_CODE) {
            context.form().setAttribute("isMoreThanFiveAttempts", true)
                    .setError(MessageConstants.SMS_LIMIT_5_CONTINUE);
            return true;
        }
        if (wroteCodeAttemptsService.getWroteCodeAttemptsByCode(protector.getPhoneNumber(), protector.getUserRealm().getName(), CODE_BY_PHONE_NUMBER.name(), codeHash) >= COUNT_BY_ONE_CODE) {
            context.form().setAttribute("isMoreThanFiveAttempts", true)
                    .setError(MessageConstants.CALL_LIMIT_5_CONTINUE);
            return true;
        }

        context.form().setAttribute("isMoreThanFiveAttempts", false);
        return false;
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
        return checkIsMoreThanFiveAttempts(context, protector);
    }

    private boolean sendIfNotBan(User user, AuthenticationFlowContext context, PhonePlusRealmProtector protector) {
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
        return true;
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
        context.form().setAttribute("expirationSeconds", String.valueOf(/*authContext.getActivationCodeType().getExpiredSeconds()*/
                activationCodeType.getExpiredSecondsToResend() - deltaTime));
    }

    private void setBlockedTime(BlackListDto blackListDto, AuthenticationSessionModel authSession, Long deltaTime, AuthenticationFlowContext context) {
        LocalDateTime unblocked = blackListDto.getUnblockedAt();
        authSession.setAuthNote(EXPIRATION_TIME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        LocalDateTime previousTime = LocalDateTime.parse(authSession.getAuthNote(EXPIRATION_TIME), DateTimeFormatter.ISO_DATE_TIME);
        deltaTime = previousTime.until(unblocked, ChronoUnit.SECONDS);
        //fixme costilya
        if (deltaTime < 0) {
            deltaTime = LocalDateTime.now().until(LocalDateTime.now().plusSeconds(blackListDto.getBlockDurationSec()), ChronoUnit.SECONDS);
        }
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

    private void addEmptyReqForB2b(AuthenticationFlowContext context, UserModel model) {
        if (context.getAuthenticationSession().getClient().getClientId().equals(CLIENT_B2B) && !model.getRequiredActions().isEmpty()) {
            addRequiredAction(context, "empty_req", model);
        }
    }

    private void doAuthActionForLogNPass(AuthenticationSessionModel sessionModel, AuthenticationFlowContext context) {
        try {
            sessionModel.setAuthNote("loginPasswordButton", "loginPasswordButton");
            sessionModel.setAuthNote(AUTH_FORM_SUCCESS, Util.TRUE_STR);
            User user = UserModelUserMapper.mapToUser(context.getUser());
            String clientId = sessionModel.getClient().getClientId();
            authorisedUsersService.saveSuccessfulAuth(user, context.getRealm().getId(), clientId, getAuthOrRegType(sessionModel));
        } catch (AuthOrRegTypeNotFoundException authOrRegTypeNotFoundException) {
            log.error(authOrRegTypeNotFoundException.getMessage());
        } finally {
            context.success();
        }
    }

    private void doAuthActionForPhone(AuthenticationSessionModel sessionModel, AuthenticationFlowContext context) {
        try {
            User user = UserModelUserMapper.mapToUser(context.getUser());
            String clientId = context.getAuthenticationSession().getClient().getClientId();
            sessionModel.removeAuthNote(CODE_HASH_KEY);
            sessionModel.removeAuthNote(EXPIRATION_TIME);
            sessionModel.removeAuthNote(COUNT_REPEAT);
            sessionModel.setAuthNote(AUTH_FORM_SUCCESS, Util.TRUE_STR);
            sessionModel.setAuthNote(sessionModel.getAuthNote("secondPhase"), "");
            sessionModel.removeAuthNote("needSendSmsCode");
            sessionModel.removeAuthNote(CODE_HASH_KEY);
            authorisedUsersService.saveSuccessfulAuth(user, context.getRealm().getId(), clientId, getAuthOrRegType(sessionModel));

        } catch (AuthOrRegTypeNotFoundException authOrRegTypeNotFoundException) {
            log.error(authOrRegTypeNotFoundException.getMessage());
        } finally {
            context.success();
        }
    }
}
