package ru.alamics.sso.keycloak.auth.form.new_auth.new_auth_flow;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.MediaType;
import ru.alamics.sso.auth_n_regi.AuthOrRegTypeNotFoundException;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.registration.model.AuthContext;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.phone.ActivationCodeType;
import ru.alamics.sso.registration.phone.UserPhoneVerifier;
import ru.alamics.sso.registration.phone.exception.*;
import ru.alamics.sso.registration.service.AuthorisedUsersService;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.Util;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;


import static ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil.getAuthOrRegType;
import static ru.alamics.sso.registration.model.UserConstants.AUTH_FORM_SUCCESS;

import static ru.alamics.sso.registration.phone.ActivationCodeType.CODE_BY_PHONE_NUMBER;
import static ru.alamics.sso.registration.phone.ActivationCodeType.CODE_TO_SMS;
import static ru.alamics.sso.registration.phone.UserPhoneVerifier.*;
import static ru.alamics.sso.registration.phone.UserPhoneVerifier.COUNT_REPEAT;
import static ru.alamics.sso.settings.SettingConstants.*;

@Slf4j
public class SmsOrPhoneCallAuth implements Authenticator {

    private static final String PAGE = "sms-phone.ftl";

    private final UserPhoneVerifier userPhoneVerifier;

    private ActivationCodeType activationCodeType;

    private static final String ERROR_CODE = "error_code";

    private final SettingsService settingsService;

    private static final String GRANT_TYPE = "grant_type";

    private final KeycloakSession keycloakSession;

    private final AuthorisedUsersService authorisedUsersService;

    public SmsOrPhoneCallAuth(UserPhoneVerifier userPhoneVerifier, KeycloakSession session) {
        this.userPhoneVerifier = userPhoneVerifier;
        this.keycloakSession = session;
        this.settingsService = Lookup.lookup(SettingsService.class);
        this.authorisedUsersService = Lookup.lookup(AuthorisedUsersService.class);
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        if (isBackButtonPressed(context)) {
            authSession.getParentSession().restartSession(context.getRealm());
            context.cancelLogin();

        } else {
            boolean isAuth = Util.TRUE_STR.equals(authSession.getAuthNote(AUTH_FORM_SUCCESS));

            if (isAuth && context.getAuthenticationSession().getAuthNote("smsOrCall") != null) {
                activationCodeType = authSession.getAuthNote("phoneCallButton") != null ? ActivationCodeType.CODE_BY_PHONE_NUMBER : CODE_TO_SMS;
                User user = UserModelUserMapper.mapToUser(context.getUser());

                AuthContext authContext = AuthContext.builder()
                        .activationCodeType(activationCodeType)
                        .expirationTime(LocalDateTime.now().plusSeconds(activationCodeType.getExpiredCodeSeconds()))
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
                            .setAttribute("expirationSeconds", String.valueOf(authContext.getActivationCodeType().getExpiredSecondsToResend()))
                            .setAttribute("lengthCode", authContext.getActivationCodeType().getLengthCode())
                            .setAttribute("activationCodeType", authContext.getActivationCodeType().name())
                            .setAttribute("enableRepeatCall", enableRepeatCall)
                            .setAttribute("sendAgain", settingsService.getSettingsStringValue(SEND_AGAIN, context.getRealm().getId()))
                            .setAttribute("doSubmit", settingsService.getSettingsStringValue(DO_SUBMIT, context.getRealm().getId()))
                            .setAttribute("homePage", settingsService.getSettingsStringValue(HOME_PAGE, context.getRealm().getId()))
                            .setAttribute("phoneConst", settingsService.getSettingsStringValue(PHONE_CONST, context.getRealm().getId()))
                            .setAttribute("footer", settingsService.getSettingsStringValue(FOOTER, context.getRealm().getId()))
                            .setAttribute("phoneConstLink", settingsService.getSettingsStringValue(PHONE_CONST_LINK, context.getRealm().getId()))
                            .setAttribute("phoneCallButton", authSession.getAuthNote("phoneCallButton") != null);

                    context.challenge(createForm(context, loginFormsProvider));

                } catch (UserPhoneEmpty userPhoneEmpty) {
                    log.info("ignore... userPhoneEmpty");
                } catch (PhoneCallException e) {
                    log.info("ignore... PhoneCallException {}", e.getMessage());
                } catch (SendMessageException se) {
                    log.info("ignore... MsgSendException {}", se.getMessage());
                }
            } else {
                context.success();
            }
        }
    }

    private boolean isBackButtonPressed(AuthenticationFlowContext context) {
        return context.getHttpRequest().getDecodedFormParameters().containsKey("on");
    }

    private Response createForm(AuthenticationFlowContext context, LoginFormsProvider loginFormsProvider) {

        String mp = context.getAuthenticationSession().getAuthNote("MP");
        String errorCode = context.getAuthenticationSession().getAuthNote(ERROR_CODE);

        if (mp != null && errorCode != null) {
            Response response = loginFormsProvider.createForm(PAGE);
            Map<String, String> entity = (Map<String, String>) response.getEntity();
            entity.put("error", "Код введен неверно. Вам выслан новый код");
            return Response.ok().entity(entity).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        if (mp != null) {
            HttpRequest contextObject = context.getSession().getContext().getContextObject(HttpRequest.class);
            MultivaluedMap<String, String> parameters = contextObject.getDecodedFormParameters();
            parameters.add(GRANT_TYPE, "password");
        }

        return loginFormsProvider.createForm(PAGE);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        ActivationCodeType.init(context.getRealm().getName());
        if (context.getHttpRequest().getDecodedFormParameters().containsKey("sendPhoneCode")) {
            log.info("Sms code send Phone");
            authSession.removeAuthNote(PHONE_KEY_HASH);

            authenticate(context);
        } else if (context.getHttpRequest().getDecodedFormParameters().containsKey("resend")) {
            log.info("Sms code resend");

            authSession.removeAuthNote(PHONE_KEY_HASH);

            authenticate(context);

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
                long codeLifeTime = 0;
                if (authContext.getActivationCodeType().equals(CODE_TO_SMS)) {
                    codeLifeTime = settingsService.getSettingsLongValue(EXPIRE_SMS_VIBER_CODE, context.getRealm().getName()) * 60; //we need seconds

                }
                if (authContext.getActivationCodeType().equals(CODE_BY_PHONE_NUMBER)) {
                    codeLifeTime = settingsService.getSettingsLongValue(EXPIRE_INCOMING_CALL_CODE, context.getRealm().getName()) * 60; //we need seconds

                }
                int typeId = 0;
                try {
                    typeId = getAuthOrRegType(authSession);
                } catch (AuthOrRegTypeNotFoundException e) {
                    typeId = 999;
                    throw new RuntimeException(e);
                }
                userPhoneVerifier.verifyPhone(user, codeLifeTime,
                        authContext.getHashProperty(), code, activationCodeType, authSession.getRealm().getName(), authSession, authorisedUsersService, typeId);

                authSession.removeAuthNote(PHONE_KEY_HASH);
                authSession.removeAuthNote(EXPIRATION_TIME);
                authSession.removeAuthNote(COUNT_REPEAT);

                context.success();

            } catch (WrongSmsCode wrongSmsCode) {
                log.warn("Wrong sms code");
                context.form()
                        .setAttribute("error", "Пароль введен не верно. Вам выслан новый код")
                        .setError("Код введен неверно. Проверьте правильность введенных данных");
                authSession.removeAuthNote(PHONE_KEY_HASH);
                authSession.setAuthNote(ERROR_CODE, ERROR_CODE);
                authenticate(context);
            } catch (TimeExpiredException e) {
                // TODO: Дополнить catch выражение
                log.warn("Expired time of code");
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }

    private Integer getCount(String countStr) {
        if (countStr == null || "null".equals(countStr)) {
            return 0;
        } else {
            return Integer.valueOf(countStr);
        }
    }
}
