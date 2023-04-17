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
    private static final Map<String, Map<VerifyPhoneKey, Integer>> counter = new HashMap<>();
    private static final Map<String, String> currentCode = new HashMap<>(); //key phone, value code
    //fixme fixme
    private static final Map<String, Integer> generalCounter = new HashMap<>(); //count all tries

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
        long deltaTime = 0L;
        AuthContext authContext = AuthContext.builder()
                .activationCodeType(activationCodeType)
                .expirationTime(LocalDateTime.now().plusSeconds(activationCodeType.getExpiredSeconds()))
                .hashProperty(authSession.getAuthNote(PHONE_KEY_HASH))
                .counter(getCount(authSession.getAuthNote(COUNT_REPEAT)))
                .build();
        if (Objects.isNull(authSession.getAuthNote(EXPIRATION_TIME))) {
            authSession.setAuthNote(EXPIRATION_TIME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        } else {
            LocalDateTime previousTime = LocalDateTime.parse(authSession.getAuthNote(EXPIRATION_TIME), DateTimeFormatter.ISO_DATE_TIME);
            deltaTime = Duration.between(previousTime, LocalDateTime.now()).getSeconds();
        }

        try {
            boolean enableRepeatCall = true;
            boolean canSendSms = false;
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
                    canSendSms = true;
                    authContext = userPhoneVerifier.sendValidationMsg(user, authContext, activationCodeType, context.getRealm());
                }
            }

            authSession.setAuthNote(PHONE_KEY_HASH, authContext.getHashProperty());
            authSession.setAuthNote(COUNT_REPEAT, authContext.getCounter().toString());

            LoginFormsProvider loginFormsProvider = context.form()
                    .setAttribute("userPhone", user.getPhone())
                    .setAttribute("userEmail", user.getEmail())
                    .setAttribute("expirationSeconds", String.valueOf(authContext.getActivationCodeType().getExpiredSeconds() - deltaTime))
                    .setAttribute("lengthCode", authContext.getActivationCodeType().getLengthCode())
                    .setAttribute("activationCodeType", authContext.getActivationCodeType().name())
                    .setAttribute("enableRepeatCall", enableRepeatCall)
                    .setAttribute("sendAgain", settingsService.getSettingsStringValue(SEND_AGAIN, context.getRealm().getId()))
                    .setAttribute("sendByEmail", settingsService.getSettingsStringValue(SEND_BY_EMAIL, context.getRealm().getId()))
                    .setAttribute("doSubmit", settingsService.getSettingsStringValue(DO_SUBMIT, context.getRealm().getId()))
                    .setAttribute("homePage", settingsService.getSettingsStringValue(HOME_PAGE, context.getRealm().getId()))
                    .setAttribute("phoneConst", settingsService.getSettingsStringValue(PHONE_CONST, context.getRealm().getId()))
                    .setAttribute("footer", settingsService.getSettingsStringValue(FOOTER, context.getRealm().getId()))
                    .setAttribute("phoneConstLink", settingsService.getSettingsStringValue(PHONE_CONST_LINK, context.getRealm().getId()))
                    .setAttribute("canSendSms", canSendSms);
            context.challenge(createForm(context, loginFormsProvider, user));

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

    private Response createForm(RequiredActionContext context, LoginFormsProvider loginFormsProvider, User user) {
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
        if (blackListService.isUserBlockedAuthBySms(user.getPhone())) {
            context.form()
                    .setError(MessageConstants.SMS_LIMIT_20_BLOCK);
        }
        if (blackListService.isUserBlockedAuthByPhoneCall(user.getPhone())) {
            context.form()
                    .setError(MessageConstants.CALL_LIMIT_20_BLOCK);
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
            currentCode.put(user.getPhone(), authContext.getHashProperty());
            authSession.removeAuthNote(PHONE_KEY_HASH);
            requiredActionChallenge(context);

        } else {
            //logic here
            Map<VerifyPhoneKey, Integer> typeCount = new HashMap<>();

            if (activationCodeType.equals(CODE_TO_SMS)) {
                checkAndAddToCounter(user, typeCount, CODE_TO_SMS, authContext);
                if (generalCounter.get(user.getPhone()) > 20) {
                    blackListService.limitUserBySmsOrPhone(user, LimitationCauseType.SMS);
                    requiredActionChallenge(context);
                    return;
                }
                verifyCode(context, authSession, user, model, authContext);
            }

            if (activationCodeType.equals(CODE_BY_PHONE_NUMBER)) {
                checkAndAddToCounter(user, typeCount, CODE_BY_PHONE_NUMBER, authContext);
                if (counter.get(user.getPhone()).values().stream().findFirst().orElseThrow(() -> new RuntimeException("counter shouldnt be null")) > 20) {
                    blackListService.limitUserBySmsOrPhone(user, LimitationCauseType.PHONE_CALL);
                    requiredActionChallenge(context);
                    return;
                }
                verifyCode(context, authSession, user, model, authContext);
            }
        }
    }

    private boolean checkIsMoreThanFiveAttempts(RequiredActionContext context, User user) {
        Map<VerifyPhoneKey, Integer> userMap = counter.get(user.getPhone());

        if (userMap == null) {
            context.form().setAttribute("isMoreThanFiveAttempts", false);
            return false;
        }

        if (userMap.entrySet().stream().anyMatch(it -> it.getKey()
                .getCurrentCode().equals(currentCode.get(user.getPhone())))) {
            if (userMap.entrySet().stream().anyMatch(it -> it.getValue() >= 5 && it.getKey().getActivationType().equals(CODE_TO_SMS))) {
                context.form().setAttribute("isMoreThanFiveAttempts", true)
                        .setError(MessageConstants.SMS_LIMIT_5_CONTINUE);
                return true;
            }
            if (userMap.entrySet().stream().anyMatch(it -> it.getValue() >= 5 && it.getKey().getActivationType().equals(CODE_BY_PHONE_NUMBER))) {
                context.form().setAttribute("isMoreThanFiveAttempts", true)
                        .setError(MessageConstants.CALL_LIMIT_5_CONTINUE);
                return true;
            }
        }
        context.form().setAttribute("isMoreThanFiveAttempts", false);
        return false;
    }

    private boolean checkCanWeSendSmS(User user, RequiredActionContext context) {
        return checkIsMoreThanFiveAttempts(context, user) || !counter.containsKey(user.getPhone()) && !blackListService.isUserBlockedAuthBySms(user.getPhone());
    }

    private void checkAndAddToCounter(User user, Map<VerifyPhoneKey, Integer> typeCount, ActivationCodeType codeByPhoneNumber, AuthContext authContext) {
        if (Objects.nonNull(authContext.getHashProperty())) {
            currentCode.put(user.getPhone(), authContext.getHashProperty()); // колво попыток 1
        }
        String code = currentCode.get(user.getPhone());

        if (!counter.containsKey(user.getPhone())) {
            typeCount.put(new VerifyPhoneKey(codeByPhoneNumber, code), 1); // колво попыток 1
            counter.put(user.getPhone(), typeCount);
            generalCounter.put(user.getPhone(), 1);
        } else {
            //fixme always start from 0 after 5 tries
            if (counter.get(user.getPhone()).entrySet().stream()
                    .allMatch(it -> it.getKey().getCurrentCode().equals(code) && it.getKey().getActivationType().equals(activationCodeType))) {
                Integer existTriesCurrentCode = counter.get(user.getPhone()).entrySet().stream().filter(it -> it.getKey().getCurrentCode().equals(code))
                        .findAny().get().getValue();
                existTriesCurrentCode++;
                typeCount.put(new VerifyPhoneKey(activationCodeType, code), existTriesCurrentCode);
                counter.put(user.getPhone(), typeCount);

                Integer existGeneralTries = generalCounter.get(user.getPhone());
                existGeneralTries++;
                generalCounter.put(user.getPhone(), existGeneralTries);
            } else {
                Integer existTries = 0;
                existTries++;
                typeCount.put(new VerifyPhoneKey(activationCodeType, code), existTries);
                counter.put(user.getPhone(), typeCount);
            }
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
            counter.remove(user.getPhone());
            currentCode.remove(user.getPhone());
            generalCounter.remove(user.getPhone());
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