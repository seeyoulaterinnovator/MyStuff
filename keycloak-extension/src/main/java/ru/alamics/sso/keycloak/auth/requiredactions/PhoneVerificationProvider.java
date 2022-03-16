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
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.registration.model.AuthContext;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static ru.alamics.sso.registration.phone.ActivationCodeType.CODE_BY_PHONE_NUMBER;
import static ru.alamics.sso.registration.phone.ActivationCodeType.CODE_TO_SMS;
import static ru.alamics.sso.registration.phone.UserPhoneVerifier.*;
import static ru.alamics.sso.settings.SettingConstants.*;

@Slf4j
public class PhoneVerificationProvider implements RequiredActionProvider {
    private static final String VERIFY_PHONE_FTL = "verifyPhone.ftl";

    private static final String NEED_SEND_EMAIL_CODE = "NEED_SEND_EMAIL_CODE";
    private static final String subject = "emailVerificationAuthSubject";
    private static final String template = "mail-verify-auth.ftl";

    private final UserPhoneVerifier userPhoneVerifier;
    private final ActivationCodeType activationCodeType;
    private final EmailTemplateProvider emailTemplateProvider;

    private final SettingsService settingsService;

    public PhoneVerificationProvider(UserPhoneVerifier userPhoneVerifier, ActivationCodeType activationCodeType, EmailTemplateProvider emailTemplateProvider) {
        this.userPhoneVerifier = userPhoneVerifier;
        this.activationCodeType = activationCodeType;
        this.emailTemplateProvider = emailTemplateProvider;
        this.settingsService = Lookup.lookup(SettingsService.class);
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

        AuthContext authContext = AuthContext.builder()
                .activationCodeType(activationCodeType)
                .expirationTime(LocalDateTime.now().plusSeconds(activationCodeType.getExpiredSeconds()))
                .hashProperty(authSession.getAuthNote(PHONE_KEY_HASH))
                .counter(getCount(authSession.getAuthNote(COUNT_REPEAT)))
                .build();

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
                authContext = userPhoneVerifier.sendValidationMsg(user, authContext, activationCodeType, context.getRealm());
            }

            authSession.setAuthNote(PHONE_KEY_HASH, authContext.getHashProperty());
            authSession.setAuthNote(EXPIRATION_TIME, authContext.getExpirationTime().format(DateTimeFormatter.ISO_DATE_TIME));
            authSession.setAuthNote(COUNT_REPEAT, authContext.getCounter().toString());

            LoginFormsProvider loginFormsProvider = context.form()
                    .setAttribute("userPhone", user.getPhone())
                    .setAttribute("userEmail", user.getEmail())
                    .setAttribute("expirationSeconds", String.valueOf(authContext.getActivationCodeType().getExpiredSeconds()))
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
        if (mp != null) {
            HttpRequest contextObject = context.getSession().getContext().getContextObject(HttpRequest.class);
            MultivaluedMap<String, String> parameters = contextObject.getDecodedFormParameters();
            parameters.add("grant_type", "password");
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
                authSession.removeAuthNote(PHONE_KEY_HASH);
                authSession.removeAuthNote(EXPIRATION_TIME);
                authSession.removeAuthNote(COUNT_REPEAT);
                context.success();
            } catch (WrongSmsCode wrongSmsCode) {
                log.warn("Wrong sms code");
                LoginFormsProvider loginFormsProvider = context.form()
                        .setAttribute("error", "Пароль введен не верно. Проверьте правильность введенных данных")
                        .setError("Введен некорректный код смс или его срок его действия истек")
                        .setAttribute("expirationSeconds", activationCodeType.getExpiredSeconds())
                        .setAttribute("lengthCode", activationCodeType.getLengthCode())
                        .setAttribute("userPhone", user.getPhone())
                        .setAttribute("userEmail", user.getEmail())
                        .setAttribute("sendAgain", settingsService.getSettingsStringValue(SEND_AGAIN, context.getRealm().getId()))
                        .setAttribute("sendByEmail", settingsService.getSettingsStringValue(SEND_BY_EMAIL, context.getRealm().getId()))
                        .setAttribute("doSubmit", settingsService.getSettingsStringValue(DO_SUBMIT, context.getRealm().getId()))
                        .setAttribute("homePage", settingsService.getSettingsStringValue(HOME_PAGE, context.getRealm().getId()))
                        .setAttribute("phoneConstLink", settingsService.getSettingsStringValue(PHONE_CONST_LINK, context.getRealm().getId()))
                        .setAttribute("phoneConst", settingsService.getSettingsStringValue(PHONE_CONST, context.getRealm().getId()))
                        .setAttribute("footer", settingsService.getSettingsStringValue(FOOTER, context.getRealm().getId()))
                        .setAttribute("enableRepeatCall", authSession.getAuthNote(NEED_SEND_EMAIL_CODE) == null);
                context.challenge(createErrorForm(context, loginFormsProvider));
            }
        }
    }

    private Response createErrorForm(RequiredActionContext context, LoginFormsProvider loginFormsProvider) {
        Response response = loginFormsProvider.createForm(VERIFY_PHONE_FTL);
        Map<String, String> entity = (Map<String, String>) response.getEntity();
        entity.put("error", "Код введен неверно, попробуйте еще раз");
        return Response.ok().entity(entity).type(MediaType.APPLICATION_JSON_TYPE).build();
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