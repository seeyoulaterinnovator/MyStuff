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
import ru.alamics.sso.registration.phone.HashGenerator;
import ru.alamics.sso.registration.phone.SmsCodeGenerator;
import ru.alamics.sso.registration.phone.UserPhoneVerifier;
import ru.alamics.sso.registration.phone.exception.PhoneCallException;
import ru.alamics.sso.registration.phone.exception.SendMessageException;
import ru.alamics.sso.registration.phone.exception.UserPhoneEmpty;
import ru.alamics.sso.registration.phone.exception.WrongSmsCode;
import ru.alamics.sso.settings.SettingsService;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static ru.alamics.sso.registration.phone.ActivationCodeType.CODE_BY_PHONE_NUMBER;
import static ru.alamics.sso.registration.phone.ActivationCodeType.CODE_TO_SMS;
import static ru.alamics.sso.registration.phone.UserPhoneVerifier.*;
import static ru.alamics.sso.settings.SettingConstants.*;

@Slf4j
public class PhoneVerificationProvider implements RequiredActionProvider {
    private static final String VERIFY_PHONE_FTL = "verifyPhone.ftl";

    private static final String NEED_SEND_EMAIL_CODE = "NEED_SEND_EMAIL_CODE";
    private static final String GRANT_TYPE = "grant_type";
    private static final String ERROR_CODE = "error_code";
    private static final String subject = "emailVerificationAuthSubject";
    private static final String template = "mail-verify-auth.ftl";

    private final UserPhoneVerifier userPhoneVerifier;
    private final ActivationCodeType activationCodeType;
    private final EmailTemplateProvider emailTemplateProvider;
    private final BlackListService blackListService;
    private final SettingsService settingsService;
    private static final Map<String, Map<VerifyPhoneKey, Integer>> mainCounter = new HashMap<>();

    public PhoneVerificationProvider(UserPhoneVerifier userPhoneVerifier, ActivationCodeType activationCodeType, EmailTemplateProvider emailTemplateProvider) {
        this.userPhoneVerifier = userPhoneVerifier;
        this.activationCodeType = activationCodeType;
        this.emailTemplateProvider = emailTemplateProvider;
        this.settingsService = Lookup.lookup(SettingsService.class);
        this.blackListService = Lookup.lookup(BlackListService.class);
        ActivationCodeType.init();
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        log.info("PhoneRequiredActionChallenge");

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        User user = UserModelUserMapper.mapToUser(context.getUser());
        long deltaTime;

        AuthContext authContext = AuthContext.builder()
                .activationCodeType(activationCodeType)
                .expirationTime(LocalDateTime.now().plusSeconds(activationCodeType.getExpiredSeconds()))
                .hashProperty(authSession.getAuthNote(PHONE_KEY_HASH))
                .counter(getCount(authSession.getAuthNote(COUNT_REPEAT)))
                .build();

        BlackListDto blackListDto = blackListService.getBlockedUser(user.getPhone());
        if (Objects.isNull(authSession.getAuthNote(EXPIRATION_TIME))) {
            authSession.setAuthNote(EXPIRATION_TIME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        if (Objects.nonNull(blackListDto)) {
            LocalDateTime unblocked = blackListDto.getUnblockedAt();
            authSession.setAuthNote(EXPIRATION_TIME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            LocalDateTime previousTime = LocalDateTime.parse(authSession.getAuthNote(EXPIRATION_TIME), DateTimeFormatter.ISO_DATE_TIME);
            deltaTime = previousTime.until(unblocked, ChronoUnit.SECONDS);
            context.form().setAttribute("expirationSeconds", String.valueOf(deltaTime))
                    .setAttribute("codeLimited", true);
        } else {
            LocalDateTime previousTime = LocalDateTime.parse(authSession.getAuthNote(EXPIRATION_TIME), DateTimeFormatter.ISO_DATE_TIME);
            deltaTime = Duration.between(previousTime, LocalDateTime.now()).getSeconds();
            context.form().setAttribute("expirationSeconds", String.valueOf(authContext.getActivationCodeType().getExpiredSeconds() - deltaTime));
        }

        try {
            boolean enableRepeatCall = true;
            if (authSession.getAuthNote(NEED_SEND_EMAIL_CODE) != null && activationCodeType.equals(CODE_BY_PHONE_NUMBER)) {
                String code = SmsCodeGenerator.getCode(ActivationCodeType.CODE_TO_EMAIL.getLengthCode());
                authContext = AuthContext.builder()
                        .activationCodeType(ActivationCodeType.CODE_TO_EMAIL)
                        .expirationTime(LocalDateTime.now().plusSeconds(ActivationCodeType.CODE_TO_EMAIL.getExpiredSeconds()))
                        .hashProperty(HashGenerator.getSecretHash(sendEmail(context, code)))
                        .counter(getCount(authSession.getAuthNote(COUNT_REPEAT)))
                        .build();
                enableRepeatCall = false;
            } else if (authSession.getAuthNote(NEED_SEND_EMAIL_CODE) != null && activationCodeType.equals(CODE_TO_SMS)) {
                String code = SmsCodeGenerator.getCode(ActivationCodeType.CODE_TO_SMS.getLengthCode());
                authContext = AuthContext.builder()
                        .activationCodeType(ActivationCodeType.CODE_TO_SMS)
                        .expirationTime(LocalDateTime.now().plusSeconds(ActivationCodeType.CODE_TO_SMS.getExpiredSeconds()))
                        .hashProperty(HashGenerator.getSecretHash(sendEmail(context, code)))
                        .counter(getCount(authSession.getAuthNote(COUNT_REPEAT)))
                        .build();
                enableRepeatCall = false;
            } else {
                if (checkCanWeSendSmS(user, context)) {
                    authContext = userPhoneVerifier.sendValidationMsg(user, authContext, activationCodeType, context.getRealm());
                }
            }

            authSession.setAuthNote(PHONE_KEY_HASH, authContext.getHashProperty());
            authSession.setAuthNote(COUNT_REPEAT, authContext.getCounter().toString());

            LoginFormsProvider loginFormsProvider = context.form()
                    .setAttribute("userPhone", user.getPhone())
                    .setAttribute("userEmail", user.getEmail())

                    .setAttribute("lengthCode", authContext.getActivationCodeType().getLengthCode())
                    .setAttribute("activationCodeType", authContext.getActivationCodeType().name())
                    .setAttribute("enableRepeatCall", enableRepeatCall)
                    .setAttribute("sendAgain", settingsService.getSettingsStringValue(SEND_AGAIN, context.getRealm().getId()))
                    .setAttribute("sendByEmail", settingsService.getSettingsStringValue(SEND_BY_EMAIL, context.getRealm().getId()))
                    .setAttribute("doSubmit", settingsService.getSettingsStringValue(DO_SUBMIT, context.getRealm().getId()))
                    .setAttribute("homePage", settingsService.getSettingsStringValue(HOME_PAGE, context.getRealm().getId()))
                    .setAttribute("phoneConst", settingsService.getSettingsStringValue(PHONE_CONST, context.getRealm().getId()))
                    .setAttribute("footer", settingsService.getSettingsStringValue(FOOTER, context.getRealm().getId()))
                    .setAttribute("phoneConstLink", settingsService.getSettingsStringValue(PHONE_CONST_LINK, context.getRealm().getId()));
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

    private Response createForm(RequiredActionContext context, LoginFormsProvider loginFormsProvider) {
        //Костыль тк при запросе с МП не нашел другого способа верификацию отправить по rest
        String mp = context.getAuthenticationSession().getAuthNote("MP");
        String errorCode = context.getAuthenticationSession().getAuthNote(ERROR_CODE);
        if (mp != null && errorCode != null) {
            Response response = loginFormsProvider.createForm(VERIFY_PHONE_FTL);
            Map<String, String> entity = (Map<String, String>) response.getEntity();
            entity.put("error", "Код введен неверно. Вам выслан новый код");
            return Response.ok().entity(entity).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        if (mp != null) {
            HttpRequest contextObject = context.getSession().getContext().getContextObject(HttpRequest.class);
            MultivaluedMap<String, String> parameters = contextObject.getDecodedFormParameters();
            parameters.add(GRANT_TYPE, "password");

        }
        return loginFormsProvider.createForm(VERIFY_PHONE_FTL);
    }


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


    @Override
    public void processAction(RequiredActionContext context) {
        log.info("PhoneProcessAction");

        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        AuthContext authContext = AuthContext.builder()
                .hashProperty(authSession.getAuthNote(PHONE_KEY_HASH))
                .expirationTime(LocalDateTime.parse(authSession.getAuthNote(EXPIRATION_TIME), DateTimeFormatter.ISO_DATE_TIME))
                .counter(getCount(authSession.getAuthNote(COUNT_REPEAT)))
                .activationCodeType(activationCodeType)
                .build();
        UserModel model = context.getUser();
        User user = UserModelUserMapper.mapToUser(model);
        //StringUtils.isNotEmpty(authContext.getHashProperty()) doesn't work
        if (Objects.nonNull(authContext.getHashProperty())) {
            authSession.setAuthNote("currentCode", authContext.getHashProperty());
        }
        /*
        форма принимает код для ввода кода из смс(6 симоволов) и 6 из почты, 4 цифры номер телефона,
        4 цифры из email
         */
        if (context.getHttpRequest().getDecodedFormParameters().containsKey("sendEmailCode")) {
            log.info("Sms code send Email");
            authSession.removeAuthNote(PHONE_KEY_HASH);
            authSession.setAuthNote(NEED_SEND_EMAIL_CODE, NEED_SEND_EMAIL_CODE);

            requiredActionChallenge(context);

        } else if (context.getHttpRequest().getDecodedFormParameters().containsKey("sendPhoneCode")) {
            log.info("Sms code send Phone");
            authSession.removeAuthNote(PHONE_KEY_HASH);
            authSession.removeAuthNote(NEED_SEND_EMAIL_CODE);
            requiredActionChallenge(context);
        } else if (context.getHttpRequest().getDecodedFormParameters().containsKey("resend")) {
            log.info("Sms code resend");

            authSession.setAuthNote(EXPIRATION_TIME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            checkAndIncrementExistTries(user, authContext, context);
            authSession.removeAuthNote(PHONE_KEY_HASH);
            requiredActionChallenge(context);

        } else {
            //logic here

            if (activationCodeType.equals(CODE_TO_SMS)) {
                checkAndAddToCounter(user, authContext, context);
                if (mainCounter.get(user.getPhone()).values().stream().findFirst().orElseThrow(() -> new RuntimeException("counter shouldnt be null")) > 20) {
                    blackListService.limitUserBySmsOrPhone(user, LimitationCauseType.SMS);
                    requiredActionChallenge(context);
                    return;
                }
                verifyCode(context, authSession, user, model, authContext);
            }

            if (activationCodeType.equals(CODE_BY_PHONE_NUMBER)) {
                checkAndAddToCounter(user, authContext, context);
                if (mainCounter.get(user.getPhone()).values().stream().findFirst().orElseThrow(() -> new RuntimeException("counter shouldnt be null")) > 20) {
                    blackListService.limitUserBySmsOrPhone(user, LimitationCauseType.PHONE_CALL);
                    requiredActionChallenge(context);
                    return;
                }
                verifyCode(context, authSession, user, model, authContext);
            }
        }
    }

    private boolean checkIsMoreThanFiveAttempts(RequiredActionContext context, User user) {
        Map<VerifyPhoneKey, Integer> userMap = mainCounter.get(user.getPhone());

        if (userMap == null) {
            context.form().setAttribute("isMoreThanFiveAttempts", false);
            return false;
        }
        String code = context.getAuthenticationSession().getAuthNote("currentCode");
        if (userMap.entrySet().stream().allMatch(it -> it.getKey()
                .getCurrentCode().equals(code) && it.getKey()
                .getCurrentCodeCounter() >= 5 && it.getKey().getActivationType().equals(CODE_TO_SMS) && it.getValue() < 20)) {
            context.form().setAttribute("isMoreThanFiveAttempts", true)
                    .setError(MessageConstants.SMS_LIMIT_5_CONTINUE);
            return true;
        }

        if (userMap.entrySet().stream().allMatch(it -> it.getKey()
                .getCurrentCode().equals(code) && it.getKey()
                .getCurrentCodeCounter() >= 5 && it.getKey().getActivationType().equals(CODE_BY_PHONE_NUMBER) && it.getValue() < 20)) {
            context.form().setAttribute("isMoreThanFiveAttempts", true)
                    .setError(MessageConstants.CALL_LIMIT_5_CONTINUE);
            return true;
        }
        context.form().setAttribute("isMoreThanFiveAttempts", false);
        return false;
    }

    private boolean checkCanWeSendSmS(User user, RequiredActionContext context) {
        if (blackListService.isUserBlockedAuthBySms(user.getPhone())) {
            context.form()
                    .setAttribute("isLimited", true)
                    .setError(MessageConstants.SMS_LIMIT_20_BLOCK);
            return false;
        }
        if (blackListService.isUserBlockedAuthByPhoneCall(user.getPhone())) {
            context.form()
                    .setAttribute("isLimited", true)
                    .setError(MessageConstants.CALL_LIMIT_20_BLOCK);
            return false;
        }
        return checkIsMoreThanFiveAttempts(context, user) || !mainCounter.containsKey(user.getPhone()) && !blackListService.isUserBlockedAuthBySms(user.getPhone());
    }

    private void checkAndAddToCounter(User user, AuthContext authContext, RequiredActionContext context) {
        if (!mainCounter.containsKey(user.getPhone()) || mainCounter.get(user.getPhone())
                .entrySet().stream().allMatch(it -> it.getKey().getCurrentCodeCounter() == null)) {
            Map<VerifyPhoneKey, Integer> initCounterMap = new HashMap<>();
            initCounterMap.put(new VerifyPhoneKey(authContext.getActivationCodeType(),
                    authContext.getHashProperty(), 0), 0 /* 0 - кол-во попыток изначально */);
            mainCounter.put(user.getPhone(), initCounterMap);
        }

        checkAndIncrementExistTries(user, authContext, context);
    }

    private void checkAndIncrementExistTries(User user, AuthContext authContext, RequiredActionContext context) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        String code = authSession.getAuthNote("currentCode");
        if (Objects.isNull(code)) {
            return;
        }
        if (mainCounter.containsKey(user.getPhone())) {
            int existCurrentCodeTries = 0;
            if (mainCounter.get(user.getPhone()).entrySet().stream().allMatch(it -> it.getKey().getCurrentCode().equals(code))) {
                existCurrentCodeTries = mainCounter.get(user.getPhone()).keySet().stream().filter(it -> it.getCurrentCode().equals(code))
                        .findAny().orElseThrow(RuntimeException::new).getCurrentCodeCounter();
            }
            existCurrentCodeTries++;
            int existUserTries = mainCounter.get(user.getPhone()).values().stream().findFirst().orElseThrow(RuntimeException::new);
            existUserTries++;

            Map<VerifyPhoneKey, Integer> currentCounterMap = new HashMap<>();
            currentCounterMap.put(new VerifyPhoneKey(authContext.getActivationCodeType(), code, existCurrentCodeTries), existUserTries);
            mainCounter.put(user.getPhone(), currentCounterMap);
        }
    }

    private void verifyCode(RequiredActionContext context, AuthenticationSessionModel authSession, User user, UserModel model, AuthContext authContext) {

        try {
            String code = context.getHttpRequest().getDecodedFormParameters().getFirst("smscode");
            userPhoneVerifier.verifyPhone(user, authContext, code, activationCodeType);

            UserModelUserMapper.mergeUserInto(user, model);
            authSession.removeAuthNote(PHONE_KEY_HASH);
            authSession.removeAuthNote(EXPIRATION_TIME);
            authSession.removeAuthNote(COUNT_REPEAT);
            context.success();
            mainCounter.remove(user.getPhone());
        } catch (WrongSmsCode wrongSmsCode) {
            log.warn("Wrong sms code");
            context.form()
                    .setAttribute("error", "Пароль введен не верно. Вам выслан новый код")
                    .setError("Код введен неверно. Проверьте правильность введенных данных");
            authSession.removeAuthNote(PHONE_KEY_HASH);
            authSession.setAuthNote(ERROR_CODE, ERROR_CODE);
            requiredActionChallenge(context);
        }
    }


    private Integer getCount(String countStr) {
        if (countStr == null || "null".equals(countStr)) {
            return 0;
        } else {
            return Integer.valueOf(countStr);
        }
    }

    @Override
    public void close() {
    }
}