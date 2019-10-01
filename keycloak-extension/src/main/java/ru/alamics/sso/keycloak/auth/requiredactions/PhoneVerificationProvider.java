package ru.alamics.sso.keycloak.auth.requiredactions;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.property.PropertyConstants;
import ru.alamics.sso.registration.model.AuthContext;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.phone.ActivationCodeType;
import ru.alamics.sso.registration.phone.HashGenerator;
import ru.alamics.sso.registration.phone.SmsCodeGenerator;
import ru.alamics.sso.registration.phone.UserPhoneVerifier;
import ru.alamics.sso.registration.phone.exception.PhoneCallException;
import ru.alamics.sso.registration.phone.exception.SmsSendException;
import ru.alamics.sso.registration.phone.exception.UserPhoneEmpty;
import ru.alamics.sso.registration.phone.exception.WrongSmsCode;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static ru.alamics.sso.keycloak.auth.TwoStepVerificationFactory.VERIFY_PHONE_FTL;
import static ru.alamics.sso.registration.phone.UserPhoneVerifier.*;

@Slf4j
public class PhoneVerificationProvider implements RequiredActionProvider {

    private static final String NEED_SEND_EMAIL_CODE = "NEED_SEND_EMAIL_CODE";
    private static final String subject = "emailVerificationAuthSubject";
    private static final String template = "mail-verify-auth.ftl";

    private final UserPhoneVerifier userPhoneVerifier;
    private final ActivationCodeType activationCodeType;
    private final EmailTemplateProvider emailTemplateProvider;

    public PhoneVerificationProvider(UserPhoneVerifier userPhoneVerifier, ActivationCodeType activationCodeType, EmailTemplateProvider emailTemplateProvider) {
        this.userPhoneVerifier = userPhoneVerifier;
        this.activationCodeType = activationCodeType;
        this.emailTemplateProvider = emailTemplateProvider;
        ApplicationProperties applicationProperties;
        try {
            InitialContext context = new InitialContext();
            applicationProperties = (ApplicationProperties) context.lookup("java:global/domru-sso/" + ApplicationProperties.class.getSimpleName());
            log.info("Got userPhoneVerifier1 from context");
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
        activationCodeType.setExpiredSeconds(Integer.parseInt(applicationProperties.getProperty(getPropertyConstants(activationCodeType), "user")));
        ActivationCodeType.CODE_TO_EMAIL.setExpiredSeconds(Integer.parseInt(applicationProperties.getProperty(PropertyConstants.EXPIRE_INCOMING_CALL_EMAIL_CODE, "user")));
    }

    private PropertyConstants getPropertyConstants(ActivationCodeType activationCodeType){
        switch (activationCodeType){
            case CODE_BY_PHONE_NUMBER: return PropertyConstants.EXPIRE_INCOMING_CALL_CODE;
            case CODE_TO_SMS: return PropertyConstants.EXPIRE_SMS_VIBER_CODE;
            case CODE_TO_EMAIL:return PropertyConstants.EXPIRE_INCOMING_CALL_EMAIL_CODE;
        }
        return null;
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        log.info("PhoneRequiredActionChallenge");

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        User user = UserModelUserMapper.mapToUser(context.getUser());

        AuthContext authContext = AuthContext.builder()
                .activationCodeType(activationCodeType)
                .expirationTime(LocalDateTime.now().plusSeconds(activationCodeType.getExpiredSeconds()))
                .hashProperty(authSession.getAuthNote(PHONE_KEY_HASH))
                .counter(getCount(authSession.getAuthNote(COUNT_REPEAT)))
                .build();

        try {
            boolean enableRepeatCall = true;
            if (authSession.getAuthNote(NEED_SEND_EMAIL_CODE) != null) {
                authContext = AuthContext.builder()
                        .activationCodeType(ActivationCodeType.CODE_TO_EMAIL)
                        .expirationTime(LocalDateTime.now().plusSeconds(ActivationCodeType.CODE_TO_EMAIL.getExpiredSeconds()))
                        .hashProperty(HashGenerator.getSecretHash(sendEmail(context)))
                        .counter(getCount(authSession.getAuthNote(COUNT_REPEAT)))
                        .build();
                enableRepeatCall = false;
            } else {
                authContext = userPhoneVerifier.sendValidationSms(user, authContext, activationCodeType);
            }

            authSession.setAuthNote(PHONE_KEY_HASH, authContext.getHashProperty());
            authSession.setAuthNote(EXPIRATION_TIME, authContext.getExpirationTime().format(DateTimeFormatter.ISO_DATE_TIME));
            authSession.setAuthNote(COUNT_REPEAT, authContext.getCounter().toString());

            Response challenge = context.form()
                    .setAttribute("userPhone", user.getPhone())
                    .setAttribute("expirationSeconds", authContext.getActivationCodeType().getExpiredSeconds())
                    .setAttribute("lengthCode", authContext.getActivationCodeType().getLengthCode())
                    .setAttribute("activationCodeType", authContext.getActivationCodeType().name())
                    .setAttribute("enableRepeatCall", enableRepeatCall)
                    .createForm(VERIFY_PHONE_FTL);

            context.challenge(challenge);

        } catch (UserPhoneEmpty userPhoneEmpty) {
            log.info("ignore... userPhoneEmpty");
        }  catch (PhoneCallException e) {
            log.info("ignore... PhoneCallException {}", e.getMessage());
        } catch (EmailException e) {
            log.info("ignore... EmailException {}", e.getMessage());
        } catch (SmsSendException se) {
            log.info("ignore... SmsSendException {}", se.getMessage());
        }
    }

    private String sendEmail(RequiredActionContext context) throws EmailException {
        String code = SmsCodeGenerator.getCode(ActivationCodeType.CODE_TO_EMAIL.getLengthCode());
        Map<String, Object> attributes = new HashMap<>();

        attributes.put("code", code);
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
        /*
        форма принимает код для ввода кода из смс(6 симоволов), 4 цифры номер телефона,
        4 цифры из email
         */
        if (context.getHttpRequest().getDecodedFormParameters().containsKey("sendEmailCode")) {

            authSession.removeAuthNote(PHONE_KEY_HASH);
            authSession.setAuthNote(NEED_SEND_EMAIL_CODE, NEED_SEND_EMAIL_CODE);

            requiredActionChallenge(context);

        } else if (context.getHttpRequest().getDecodedFormParameters().containsKey("resend")) {
            log.info("Sms code resend");

            authSession.removeAuthNote(PHONE_KEY_HASH);

            requiredActionChallenge(context);

        } else {
            UserModel model = context.getUser();
            User user = UserModelUserMapper.mapToUser(model);

            AuthContext authContext = AuthContext.builder()
                    .hashProperty(authSession.getAuthNote(PHONE_KEY_HASH))
                    .expirationTime(LocalDateTime.parse(authSession.getAuthNote(EXPIRATION_TIME), DateTimeFormatter.ISO_DATE_TIME))
                    .counter(getCount(authSession.getAuthNote(COUNT_REPEAT)))
                    .activationCodeType(activationCodeType)
                    .build();

            try {
                String code = context.getHttpRequest().getDecodedFormParameters().getFirst("smscode");

                userPhoneVerifier.verifyPhone(user, authContext, code, activationCodeType);

                UserModelUserMapper.mergeUserInto(user, model);
                authSession.removeAuthNote(NEED_SEND_EMAIL_CODE);
                authSession.removeAuthNote(PHONE_KEY_HASH);
                authSession.removeAuthNote(EXPIRATION_TIME);
                authSession.removeAuthNote(COUNT_REPEAT);
                context.success();
            } catch (WrongSmsCode wrongSmsCode) {
                log.warn("Wrong sms code");
                Response challenge = context.form()
                        .setAttribute("error", "Пароль введен не верно. Проверьте правильность введенных данных")
                        .setError("Введен некорректный код смс или его срок его действия истек")
                        .setAttribute("expirationSeconds", activationCodeType.getExpiredSeconds())
                        .setAttribute("lengthCode", activationCodeType.getLengthCode())
                        .setAttribute("userPhone", user.getPhone())
                        .createForm(VERIFY_PHONE_FTL);
                context.challenge(challenge);
            }
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