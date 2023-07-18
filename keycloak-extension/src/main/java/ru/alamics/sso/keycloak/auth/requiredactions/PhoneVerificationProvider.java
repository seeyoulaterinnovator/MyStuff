package ru.alamics.sso.keycloak.auth.requiredactions;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.MediaType;
import ru.alamics.sso.antifraud.*;
import ru.alamics.sso.keycloak.auth.form.new_auth.PhonePlusRealmProtector;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.keycloak.util.PhoneVerifierUtil;
import ru.alamics.sso.keycloak.util.UserToUserEntityMapper;
import ru.alamics.sso.keycloak.util.VerifyPhoneKey;
import ru.alamics.sso.registration.model.MessageConstants;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.phone.ActivationCodeType;
import ru.alamics.sso.registration.phone.HashGenerator;
import ru.alamics.sso.registration.phone.SmsCodeGenerator;
import ru.alamics.sso.registration.phone.UserPhoneVerifier;
import ru.alamics.sso.registration.phone.exception.*;
import ru.alamics.sso.registration.phone.port.PhoneCallerRemoteService;
import ru.alamics.sso.registration.phone.port.SendMessageService;
import ru.alamics.sso.settings.SettingsService;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static ru.alamics.sso.registration.phone.ActivationCodeType.*;
import static ru.alamics.sso.registration.phone.UserPhoneVerifier.COUNT_REPEAT;
import static ru.alamics.sso.registration.phone.UserPhoneVerifier.EXPIRATION_TIME;
import static ru.alamics.sso.settings.SettingConstants.*;

@Slf4j
public class PhoneVerificationProvider implements RequiredActionProvider {
    private static final String VERIFY_PHONE_FTL = "verifyPhone.ftl";
    public static final String CODE_EXPIRATION_TIME = "CODE_EXPIRATION_TIME";
    public static final String BLOCK_EXPIRATION_TIME = "BLOCK_EXPIRATION_TIME";
    private static final String NEED_SEND_EMAIL_CODE = "NEED_SEND_EMAIL_CODE";
    private static final String NEED_SWITCH_TO_SMS_CODE = "NEED_SWITCH_TO_SMS_CODE";
    private static final String NEED_SEND_SMS_CODE_OR_DO_CALL = "NEED_SEND_SMS_CODE";
    private static final String CODE_HASH_KEY = "CODE_HASH_KEY";
    private static final String USER_BLOCKED = "USER_BLOCKED";
    private static final String GRANT_TYPE = "grant_type";
    private static final String ERROR_CODE = "error_code";
    private static final String subject = "emailVerificationAuthSubject";
    private static final String template = "mail-verify-auth.ftl";

    private static final String IS_MORE_THAN_FIVE_ATTEMPTS = "isMoreThanFiveAttempts";
    private static final String CODE_LIMITED = "codeLimited";

    private static final int MAX_RESEND_TRIES = 4; //на самом деле 5
    private static final int ONE_CODE_ATTEMPTS = 5;
    public static final String MESSENGER = "messenger";

    private final UserPhoneVerifier userPhoneVerifier;
    private ActivationCodeType activationCodeType;
    private final EmailTemplateProvider emailTemplateProvider;
    private final BlackListService blackListService;
    private final SettingsService settingsService;
    private final AttemptFailsService attemptFailsService;
//    private static final Map<PhonePlusRealmProtector, VerifyPhoneKey> mainCounter = new HashMap<>(); // key userPhone
//    private static final ConcurrentHashMap<PhonePlusRealmProtector, Integer> lastAttemptCounter = new ConcurrentHashMap<>();

    private final SendMessageService messageSendService;
    private final PhoneCallerRemoteService phoneCallerService;
    private final WroteCodeAttemptsService wroteCodeAttemptsService;

    public PhoneVerificationProvider(UserPhoneVerifier userPhoneVerifier, ActivationCodeType activationCodeType, EmailTemplateProvider emailTemplateProvider) {
        this.userPhoneVerifier = userPhoneVerifier;
        this.activationCodeType = activationCodeType;
        this.emailTemplateProvider = emailTemplateProvider;
        this.settingsService = Lookup.lookup(SettingsService.class);
        this.blackListService = Lookup.lookup(BlackListService.class);
        this.messageSendService = Lookup.lookup(SendMessageService.class, "MessageSender");
        this.phoneCallerService = Lookup.lookup(PhoneCallerRemoteService.class, "PhoneCallerService");
        this.attemptFailsService = Lookup.lookup(AttemptFailsService.class);
        this.wroteCodeAttemptsService = Lookup.lookup(WroteCodeAttemptsService.class);
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        log.info("PhoneRequiredActionChallenge");

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        String userPhone = UserModelUserMapper.mapToUser(context.getUser()).getPhone();
        PhonePlusRealmProtector protector = new PhonePlusRealmProtector(userPhone, context.getRealm());
        // Переключаемся на отправку кода по СМС, даже если activationCodeType = CODE_BY_PHONE_NUMBER
        if (authSession.getAuthNote(NEED_SWITCH_TO_SMS_CODE) != null) {
            activationCodeType = CODE_TO_SMS;
        }

        try {
            if (userPhone == null || userPhone.isEmpty())
                throw new UserPhoneEmpty();

            // Если телефона в божественной мапе нету, то мы должны послать СМС или ДОЗВОН
            if (Objects.isNull(authSession.getAuthNote(CODE_HASH_KEY)) || authSession.getAuthNote(CODE_HASH_KEY).equals("")) {
                authSession.setAuthNote(NEED_SEND_SMS_CODE_OR_DO_CALL, NEED_SEND_SMS_CODE_OR_DO_CALL);
            }

            LocalDateTime codeExpirationTime = getCodeExpirationTime(context); // Переменная отвечающая за временную точку, когда истечёт действие кода
            LocalDateTime blockExpirationTime = getExpirationBlockTime(context); // Переменная отвечающая за временную точку, когда пользовать будет разблокирован
            LocalDateTime expireTime = getExpireTime(context);

            // Если больше 5-ти попыток, то код истекает сейчас
            if (isMoreThanFiveAttempts(context, protector)) {
                codeExpirationTime = LocalDateTime.now();
                context.getAuthenticationSession().setAuthNote(CODE_EXPIRATION_TIME, codeExpirationTime.format(DateTimeFormatter.ISO_DATE_TIME));
            }

            if (authSession.getAuthNote(NEED_SEND_EMAIL_CODE) != null){
                activationCodeType = CODE_TO_EMAIL;
            }

            boolean isUserBlocked = isUserBlocked(context);
            boolean needWeSendSmsOrDoCall = needWeSendSmsOrDoCall(context);

            switch (activationCodeType) {
                case CODE_TO_SMS:
                    if (!isUserBlocked){
                        if (needWeSendSmsOrDoCall){
                            String code = SmsCodeGenerator.getCode(activationCodeType.getLengthCode());
                            authSession.setAuthNote(CODE_HASH_KEY, HashGenerator.getSecretHash(code));

                            String[] messengerList = context.getRealm().getSmtpConfig().get(MESSENGER).split(",");
                            messageSendService.sendMessageToMessengers(userPhone, code, context.getRealm().getId(), messengerList);

                            codeExpirationTime = setCodeExpirationTime(context); // Устанавливаем новую временную точку, когда истечёт действие кода
                            expireTime = setExpirationTime(context); // Таймер до кнопки отправить ещё раз
                            authSession.removeAuthNote(NEED_SEND_SMS_CODE_OR_DO_CALL);
                        }
                    }
                    break;
                case CODE_BY_PHONE_NUMBER:
                    if (!isUserBlocked){
                        if (needWeSendSmsOrDoCall){
                            String code = phoneCallerService.callAndGetCode(userPhone, 1);
                            authSession.setAuthNote(CODE_HASH_KEY, HashGenerator.getSecretHash(code));

                            codeExpirationTime = setCodeExpirationTime(context); // Устанавливаем новую временную точку, когда истечёт действие кода
                            expireTime = setExpirationTime(context); // Таймер до кнопки отправить ещё раз
                            authSession.removeAuthNote(NEED_SEND_SMS_CODE_OR_DO_CALL);
                        }
                    }
                    break;
                case CODE_TO_EMAIL: // Недостижимый функционал
                    String code = SmsCodeGenerator.getCode(activationCodeType.getLengthCode());
                    code = sendEmail(context, code);

                    expireTime = setExpirationTime(context); // Таймер до кнопки отправить ещё раз

                    authSession.setAuthNote(CODE_HASH_KEY, HashGenerator.getSecretHash(code));
                    break;
                default:
                    throw new RuntimeException("Unsupported  verification format"); // TODO: create good Error
            }

            LoginFormsProvider loginFormsProvider = context.form()
                    .setAttribute("userPhone", userPhone)
                    .setAttribute("secondsCodeIsValid", ChronoUnit.SECONDS.between(LocalDateTime.now(), expireTime))
                    .setAttribute("secondsUserIsBlocked", ChronoUnit.SECONDS.between(LocalDateTime.now(), blockExpirationTime))
                    .setAttribute("lengthCode", activationCodeType.getLengthCode())
                    .setAttribute("activationCodeType", activationCodeType.name())
                    .setAttribute("isRegistration", authSession.getAuthNote("REGISTRATION") != null)
                    .setAttribute("sendAgain", settingsService.getSettingsStringValue(SEND_AGAIN, context.getRealm().getId()))
                    .setAttribute("sendByEmail", settingsService.getSettingsStringValue(SEND_BY_EMAIL, context.getRealm().getId()))
                    .setAttribute("doSubmit", settingsService.getSettingsStringValue(DO_SUBMIT, context.getRealm().getId()))
                    .setAttribute("homePage", settingsService.getSettingsStringValue(HOME_PAGE, context.getRealm().getId()))
                    .setAttribute("phoneConst", settingsService.getSettingsStringValue(PHONE_CONST, context.getRealm().getId()))
                    .setAttribute("footer", settingsService.getSettingsStringValue(FOOTER, context.getRealm().getId()))
                    .setAttribute("phoneConstLink", settingsService.getSettingsStringValue(PHONE_CONST_LINK, context.getRealm().getId()))
                    .setAttribute("secondPhaseLogin", isLoginSecondPhaseActivated(context))
                    .setAttribute("smsMessage", context.getUser().getRequiredActions().contains("phone_verificator_sms"))
                    .setAttribute("enableRepeatCall", true);// just for stelecom

            context.challenge(createForm(context, loginFormsProvider));

        } catch (UserPhoneEmpty userPhoneEmpty) {
            log.info("ignore... userPhoneEmpty");
        } catch (PhoneCallException e) {
            log.info("ignore... PhoneCallException {}", e.getMessage());
        } catch (EmailException e) {
            log.info("ignore... EmailException {}", e.getMessage());
        } catch (SendMessageException se) {
            log.info("ignore... MsgSendException {}", se.getMessage());
        }
    }

    @Override
    public void processAction(RequiredActionContext context) {
        log.info("PhoneProcessAction");
        ActivationCodeType.init(context.getRealm().getName());
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        PhonePlusRealmProtector protector = new PhonePlusRealmProtector(UserModelUserMapper.mapToUser(context.getUser()).getPhone(),
                context.getRealm());
        // Переключаемся на отправку кода по СМС, даже если activationCodeType = CODE_BY_PHONE_NUMBER
        if (authSession.getAuthNote(NEED_SWITCH_TO_SMS_CODE) != null) {
            activationCodeType = CODE_TO_SMS;
        }

        String codeHash = authSession.getAuthNote(CODE_HASH_KEY);

        if (context.getHttpRequest().getDecodedFormParameters().containsKey("sendPhoneCode")) {
            log.info("Sms code send");
            activationCodeType = CODE_TO_SMS;
            authSession.setAuthNote(NEED_SWITCH_TO_SMS_CODE, NEED_SWITCH_TO_SMS_CODE);
            authSession.setAuthNote(NEED_SEND_SMS_CODE_OR_DO_CALL, NEED_SEND_SMS_CODE_OR_DO_CALL);
            checkIsLimited(context, protector, authSession, codeHash);
            requiredActionChallenge(context);
        } else if (context.getHttpRequest().getDecodedFormParameters().containsKey("sendEmailCode")) {
            log.info("Email code send");
            activationCodeType = CODE_TO_EMAIL;
            requiredActionChallenge(context);
        } else if (context.getHttpRequest().getDecodedFormParameters().containsKey("again")) {
            String userPhone = UserModelUserMapper.mapToUser(context.getUser()).getPhone();
            if (codeHash != null && !codeHash.equals("")) {
                if (activationCodeType.equals(CODE_BY_PHONE_NUMBER)) {
                    attemptFailsService.saveAttempt(new AttemptFailsDto(userPhone, codeHash, context.getRealm().getName(), CODE_BY_PHONE_NUMBER.name(), LocalDateTime.now(), context.getUser().getId()));
                }

                if (activationCodeType.equals(CODE_TO_SMS)) {
                    attemptFailsService.saveAttempt(new AttemptFailsDto(userPhone, codeHash, context.getRealm().getName(), CODE_TO_SMS.name(), LocalDateTime.now(), context.getUser().getId()));
                }
            }
            checkIsLimited(context, protector, authSession, codeHash);

            log.info("Sms code resend");
            authSession.setAuthNote(NEED_SEND_SMS_CODE_OR_DO_CALL, NEED_SEND_SMS_CODE_OR_DO_CALL);
            requiredActionChallenge(context);
        } else {
            UserModel model = context.getUser();
            User user = UserModelUserMapper.mapToUser(model);

            if (authSession.getAuthNote(USER_BLOCKED) != null) {
                requiredActionChallenge(context);
                return;
            }
            String code = context.getHttpRequest().getDecodedFormParameters().getFirst("smscode");
            try {
                /*LocalDateTime codeExpirationTime = LocalDateTime.parse(authSession.getAuthNote(CODE_EXPIRATION_TIME), DateTimeFormatter.ISO_DATE_TIME);*/

                userPhoneVerifier.verifyPhone(user, activationCodeType.getExpiredCodeSeconds(),
                        codeHash, code, activationCodeType, authSession.getRealm().getName(), authSession);

                UserModelUserMapper.mergeUserInto(user, model);
                authSession.removeAuthNote(CODE_HASH_KEY);
                authSession.removeAuthNote(CODE_EXPIRATION_TIME);
                authSession.removeAuthNote(COUNT_REPEAT);
                context.success();
            } catch (WrongSmsCode wrongSmsCode) {
                log.warn("Wrong sms code");
                context.form()
                        .setError("Код введен неверно. Проверьте правильность введенных данных");
                authSession.setAuthNote(ERROR_CODE, ERROR_CODE);
                wroteCodeAttemptsService.saveFailWroteCode(codeHash, code, protector.getPhoneNumber(), protector.getUserRealm().getName(), activationCodeType.name(), user);
                requiredActionChallenge(context);
            } catch (TimeExpiredException e) { // TODO: FIX duplicate code at the catch
                log.warn("Time for code is expired");
                context.form()
                        .setError("Истёк срок действия кода");
                authSession.setAuthNote(ERROR_CODE, ERROR_CODE);
                requiredActionChallenge(context);
            }
        }
    }

    @Override
    public void close() {
    }

    public void checkIsLimited(RequiredActionContext context, PhonePlusRealmProtector protector, AuthenticationSessionModel authSession, String code) {
        User user = UserModelUserMapper.mapToUser(context.getUser());

        if (activationCodeType.equals(CODE_TO_SMS)) {
            List<AttemptFailsDto> smsAttempts = attemptFailsService.getAttempts(user.getPhone(), context.getRealm().getName(), CODE_TO_SMS.name(), UserToUserEntityMapper.toUserEntity(user));
            if (!smsAttempts.isEmpty() && smsAttempts.size() >= MAX_RESEND_TRIES && !blackListService.isUserBlockedAuthBySms(user.getPhone(), context)) {
                if (wroteCodeAttemptsService.getWroteCodeAttemptsByCode(protector.getPhoneNumber(), protector.getUserRealm().getName(), CODE_TO_SMS.name(), code) >= 5) {
                    blackListService.limitUserBySmsOrPhone(user, activationCodeType.name(), authSession);
                    requiredActionChallenge(context);
                    return;
                }
            }
        }

        if (activationCodeType.equals(CODE_BY_PHONE_NUMBER)) {
            List<AttemptFailsDto> callAttempts = attemptFailsService.getAttempts(user.getPhone(), context.getRealm().getName(), CODE_BY_PHONE_NUMBER.name(), UserToUserEntityMapper.toUserEntity(user));
            if (!callAttempts.isEmpty() && callAttempts.size() >= MAX_RESEND_TRIES && !blackListService.isUserBlockedAuthByPhoneCall(user.getPhone(), context)) {
                if (wroteCodeAttemptsService.getWroteCodeAttemptsByCode(protector.getPhoneNumber(), protector.getUserRealm().getName(), CODE_BY_PHONE_NUMBER.name(), code) >= 5) {
                    blackListService.limitUserBySmsOrPhone(user, activationCodeType.name(), context.getAuthenticationSession());
                    requiredActionChallenge(context);
                }
            }
        }
    }


    private LocalDateTime getCodeExpirationTime(RequiredActionContext context) {
        String codeExpirationTime = context.getAuthenticationSession().getAuthNote(CODE_EXPIRATION_TIME);
        if (codeExpirationTime != null)
            return LocalDateTime.parse(codeExpirationTime, DateTimeFormatter.ISO_DATE_TIME);
        return LocalDateTime.now();
    }

    private LocalDateTime setCodeExpirationTime(RequiredActionContext context) {
        LocalDateTime codeExpirationTime = LocalDateTime.now().plusSeconds(activationCodeType.getExpiredCodeSeconds());
        context.getAuthenticationSession().setAuthNote(CODE_EXPIRATION_TIME, codeExpirationTime.format(DateTimeFormatter.ISO_DATE_TIME));

        return codeExpirationTime;
    }

    private LocalDateTime getExpireTime(RequiredActionContext context) {
        String expireTime = context.getAuthenticationSession().getAuthNote(EXPIRATION_TIME);
        if (expireTime !=null) {
            return LocalDateTime.parse(expireTime, DateTimeFormatter.ISO_DATE_TIME);
        }
        return LocalDateTime.now();
    }

    private LocalDateTime setExpirationTime(RequiredActionContext context) {

        LocalDateTime expirationTime = LocalDateTime.now().plusSeconds(activationCodeType.getExpiredSecondsToResend());
        context.getAuthenticationSession().setAuthNote(EXPIRATION_TIME, expirationTime.format(DateTimeFormatter.ISO_DATE_TIME));
        return expirationTime;
    }

    private LocalDateTime getExpirationBlockTime(RequiredActionContext context) {
        String userPhone = UserModelUserMapper.mapToUser(context.getUser()).getPhone();

        // TODO: постоянно ходим в БД, чтобы найти юзера, вроде это плохо. Нужно от этого избавиться с помощью кеширования в контексте
        BlackListDto blockedUser = blackListService.getBlockedUser(userPhone, context);
        if (blockedUser != null &&
                (activationCodeType.equals(CODE_TO_SMS) && blackListService.isUserBlockedAuthBySms(userPhone, context) ||
                        activationCodeType.equals(CODE_BY_PHONE_NUMBER) && blackListService.isUserBlockedAuthByPhoneCall(userPhone, context))
        ) {
            LocalDateTime expirationBlockTime = blockedUser.getUnblockedAt();
            context.getAuthenticationSession().setAuthNote(BLOCK_EXPIRATION_TIME, expirationBlockTime.format(DateTimeFormatter.ISO_DATE_TIME));
            return expirationBlockTime;
        }

        context.getAuthenticationSession().setAuthNote(BLOCK_EXPIRATION_TIME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return LocalDateTime.now();
    }

    private boolean isLoginSecondPhaseActivated(RequiredActionContext context) {
        return context.getAuthenticationSession().getAuthNote("smsButton") != null
                || (context.getAuthenticationSession().getAuthNote("phoneCallButton") != null)
                || (context.getAuthenticationSession().getAuthNote("loginPasswordButton") != null);
    }

    private Response createForm(RequiredActionContext context, LoginFormsProvider loginFormsProvider) {
        //Костыль тк при запросе с МП не нашел другого способа верификацию отправить по rest
        String mp = context.getAuthenticationSession().getAuthNote("MP");
        String errorCode = context.getAuthenticationSession().getAuthNote(ERROR_CODE);

        if (mp != null && errorCode != null) {
            Response response = loginFormsProvider.createForm(VERIFY_PHONE_FTL);
            Map<String, String> entity = (Map<String, String>) response.getEntity();
            entity.put("error", "Код введен неверно. Вам выслан новый код1");
            return Response.ok().entity(entity).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        if (mp != null) {
            HttpRequest contextObject = context.getSession().getContext().getContextObject(HttpRequest.class);
            MultivaluedMap<String, String> parameters = contextObject.getDecodedFormParameters();
            parameters.add(GRANT_TYPE, "password");
        }
        return loginFormsProvider.createForm(VERIFY_PHONE_FTL);
    }

    // TODO: вынести этот метод в отдельный сервис
    private String sendEmail(RequiredActionContext context, String code) throws EmailException {
        Map<String, Object> attributes = new HashMap<>();

        attributes.put("code", code);
        attributes.put("phoneInMessage", settingsService.getSettingsStringValue(PHONE_IN_MESSAGE, context.getRealm().getId()));
        attributes.put("footerInMassage", settingsService.getSettingsStringValue(FOOTER_IN_MESSAGE, context.getRealm().getId()));
        attributes.put("customer", settingsService.getSettingsStringValue(CUSTOMER, context.getRealm().getId()));
        attributes.put("gratitudeUp", settingsService.getSettingsStringValue(GRATITUDE_UP, context.getRealm().getId()));
        attributes.put("gratitudeDown", settingsService.getSettingsStringValue(GRATITUDE_DOWN, context.getRealm().getId()));
        attributes.put("phoneConstLink", settingsService.getSettingsStringValue(PHONE_CONST_LINK, context.getRealm().getId()));
        attributes.put("homePage", settingsService.getSettingsStringValue(HOME_PAGE, context.getRealm().getId()));
        attributes.put("emailVerificationAuthBodyHtml", settingsService.getSettingsStringValue(EMAIL_VERIFICATION_AUTH_ACCOUNT, context.getRealm().getId()));
        emailTemplateProvider
                .setRealm(context.getRealm())
                .setUser(context.getUser())
                .send(subject, template, attributes);
        return code;
    }

    private boolean isMoreThanFiveAttempts(RequiredActionContext context, PhonePlusRealmProtector protector) {

        // Это чтоб ошибки не проставлялись при нажатии кнопки "Отправить ещё раз"
        if (needWeSendSmsOrDoCall(context)) {
            return false;
        }

        String codeHash = context.getAuthenticationSession().getAuthNote(CODE_HASH_KEY);

        if (wroteCodeAttemptsService.getWroteCodeAttemptsByCode(protector.getPhoneNumber(), protector.getUserRealm().getName(), CODE_TO_SMS.name(), codeHash) >= ONE_CODE_ATTEMPTS) {
            context.form().setAttribute("isMoreThanFiveAttempts", true)
                    .setError(MessageConstants.SMS_LIMIT_5_CONTINUE);
            return true;
        }
        if (wroteCodeAttemptsService.getWroteCodeAttemptsByCode(protector.getPhoneNumber(), protector.getUserRealm().getName(), CODE_BY_PHONE_NUMBER.name(), codeHash) >= ONE_CODE_ATTEMPTS) {
            context.form().setAttribute("isMoreThanFiveAttempts", true)
                    .setError(MessageConstants.CALL_LIMIT_5_CONTINUE);
            return true;
        }

        return false;
    }

    private boolean isUserBlocked(RequiredActionContext context) {
        String userPhone = UserModelUserMapper.mapToUser(context.getUser()).getPhone();
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        if (activationCodeType.equals(CODE_TO_SMS) && blackListService.isUserBlockedAuthBySms(userPhone, context)) {
            context.form().setAttribute("codeLimited", true).setAttribute("isMoreThanFiveAttempts", false).setError(MessageConstants.SMS_LIMIT_BLOCK);
            authSession.setAuthNote(USER_BLOCKED, USER_BLOCKED);
            return true;
        }

        if (activationCodeType.equals(CODE_BY_PHONE_NUMBER) && blackListService.isUserBlockedAuthByPhoneCall(userPhone, context)) {
            context.form().setAttribute("codeLimited", true).setAttribute("isMoreThanFiveAttempts", false).setError(MessageConstants.CALL_LIMIT_BLOCK);
            authSession.setAuthNote(USER_BLOCKED, USER_BLOCKED);
            return true;
        }

        authSession.removeAuthNote(USER_BLOCKED);
        return false;
    }

    private boolean needWeSendSmsOrDoCall(RequiredActionContext context) {
        return context.getAuthenticationSession().getAuthNote(NEED_SEND_SMS_CODE_OR_DO_CALL) != null;
    }

}