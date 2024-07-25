package ru.alamics.sso.keycloak.auth.rest;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.MediaType;
import ru.alamics.sso.antifraud.*;
import ru.alamics.sso.jpa.entity.auth_reg.AuthOrRegType;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticator;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.keycloak.util.MiscUtil;
import ru.alamics.sso.keycloak.util.UserToUserEntityMapper;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.model.UserConstants;
import ru.alamics.sso.registration.phone.ActivationCodeType;
import ru.alamics.sso.registration.phone.HashGenerator;
import ru.alamics.sso.registration.phone.SmsCodeGenerator;
import ru.alamics.sso.registration.phone.UserPhoneVerifier;
import ru.alamics.sso.registration.phone.exception.TimeExpiredException;
import ru.alamics.sso.registration.phone.exception.WrongSmsCode;
import ru.alamics.sso.registration.phone.port.PhoneCallerRemoteService;
import ru.alamics.sso.registration.phone.port.SendMessageService;
import ru.alamics.sso.registration.service.AuthorisedUsersService;

import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ru.alamics.sso.registration.phone.UserPhoneVerifier.EXPIRATION_TIME;
import static ru.alamics.sso.registration.phone.UserPhoneVerifier.MESSENGER;

/**
 * За основу взят {@link ru.alamics.sso.keycloak.auth.form.new_auth.NewAbstractAuthMailPhoneForm}.
 * Требует flow:
 * <ul>
 *     <li>{@link ru.alamics.sso.keycloak.auth.form.new_auth.new_rest.auth.UserNameOrPhoneRestValidator}</li>
 *     <li>{@link ru.alamics.sso.keycloak.auth.form.new_auth.new_rest.auth.PasswordRestValidator}</li>
 *     <li>{@link RestSmsOrPhoneCallAuth}</li>
 *     <li>{@link RestRequiredActionsAuthenticator}</li>
 * </ul>
 */
@Slf4j
public class RestSmsOrPhoneCallAuth extends AbstractAuthenticator {
    private final BlackListService blackListService;

    private final AttemptFailsService attemptFailsService;

    private final SendMessageService messageSendService;

    private final PhoneCallerRemoteService phoneCallerService;

    private final WroteCodeAttemptsService wroteCodeAttemptsService;

    private final UserPhoneVerifier userPhoneVerifier;

    private final AuthorisedUsersService authorisedUsersService;

    public RestSmsOrPhoneCallAuth() {
        this.blackListService = Lookup.lookup(BlackListService.class);
        this.attemptFailsService = Lookup.lookup(AttemptFailsService.class);
        this.messageSendService = Lookup.lookup(SendMessageService.class, "MessageSender");
        this.phoneCallerService = Lookup.lookup(PhoneCallerRemoteService.class, "PhoneCallerService");
        this.wroteCodeAttemptsService = Lookup.lookup(WroteCodeAttemptsService.class);
        this.userPhoneVerifier = Lookup.lookup(UserPhoneVerifier.class);
        this.authorisedUsersService = Lookup.lookup(AuthorisedUsersService.class);
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        AuthenticationSessionModel session = context.getAuthenticationSession();
        UserModel userModel = context.getUser();
        User user = UserModelUserMapper.mapToUser(context.getUser());
        List<AuthOrRegType> authOrRegTypes = context.getUser()
                .getRequiredActions()
                .stream()
                .map(AuthOrRegType::findByReqActProviderName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        ActivationCodeType codeType = authOrRegTypes.contains(AuthOrRegType.SMS_CODE) ? ActivationCodeType.CODE_TO_SMS :
                authOrRegTypes.contains(AuthOrRegType.PHONE_CALL) ? ActivationCodeType.CODE_BY_PHONE_NUMBER : null;
        AuthOrRegType authType = codeType == ActivationCodeType.CODE_TO_SMS ? AuthOrRegType.SMS_CODE :
                AuthOrRegType.PHONE_CALL;

        // Config
        Map<String, String> config = context.getAuthenticatorConfig() != null ?
                context.getAuthenticatorConfig().getConfig() : Collections.emptyMap();
        int maxResendRecallTries = MiscUtil.parsInt(
                config.get(RestSmsOrPhoneCallAuthFactory.MAX_RESEND_RECALL_TRIES.getName()),
                (int) RestSmsOrPhoneCallAuthFactory.MAX_RESEND_RECALL_TRIES.getDefaultValue()
        );
        int countByOnCode = MiscUtil.parsInt(
                config.get(RestSmsOrPhoneCallAuthFactory.COUNT_BY_ONE_CODE.getName()),
                (int) RestSmsOrPhoneCallAuthFactory.COUNT_BY_ONE_CODE.getDefaultValue()
        );
        Duration newSendDelay = Duration.ofSeconds(MiscUtil.parsInt(
                config.get(RestSmsOrPhoneCallAuthFactory.NEW_SEND_DELAY_SECS.getName()),
                (int) RestSmsOrPhoneCallAuthFactory.NEW_SEND_DELAY_SECS.getDefaultValue()
        ));
        Duration blockCacheExpire = Duration.ofSeconds(MiscUtil.parseLong(
                config.get(RestSmsOrPhoneCallAuthFactory.BLOCK_CHECK_CACHE_SECS.getName()),
                (int) RestSmsOrPhoneCallAuthFactory.BLOCK_CHECK_CACHE_SECS.getDefaultValue()
        ));

        // Parameters
        String host = context.getHttpRequest().getUri().getBaseUri().getHost();
        String userCode = context.getHttpRequest().getDecodedFormParameters().getFirst("smscode");
        boolean isPassword = context.getHttpRequest().getDecodedFormParameters().containsKey(CredentialRepresentation.PASSWORD);

        // Attributes
        String codeHashKey = userModel.getFirstAttribute(UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_CODE_HASH_KEY);
        Instant codeSentAt = MiscUtil.parseInstant(userModel.getFirstAttribute(
                UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_CODE_SENT_AT));
        Instant blockedAt = Instant.ofEpochMilli(MiscUtil.parseLong(userModel.getFirstAttribute(
                UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_BLOCKED_AT), 0));

        if(codeType == null || isPassword) {
            context.success();
            return;
        }

        if(user.getPhone() == null) {
            failure(context, "У пользователя нет телефона");
            return;
        }

        boolean isBlockCached = Instant.now().isBefore(blockedAt);
        boolean isBlocked = isBlockCached;
        if(!isBlocked) {
            BlackListDto blackList = blackListService.getBlockedUser(user.getPhone(), context);
            if (blackList != null && (
                    codeType == ActivationCodeType.CODE_TO_SMS
                            && blackListService.isUserBlockedAuthBySms(user.getPhone(), context)
                            || codeType == ActivationCodeType.CODE_BY_PHONE_NUMBER
                            && blackListService.isUserBlockedAuthByPhoneCall(user.getPhone(), context)
            )) {
                isBlocked = true;
            }
        }
        if(!isBlocked) {
            long attemptCount = attemptFailsService.getAttempts(
                    user.getPhone(), context.getRealm().getName(), codeType.name(),
                    UserToUserEntityMapper.toUserEntity(user)
            ).size();
            if(attemptCount > maxResendRecallTries) {
                blackListService.limitUserBySmsOrPhoneV2(user, codeType.name(), session);
                log.info("{} blocked by attempt count", user.getPhone());
                isBlocked = true;
            }
        }
        if(isBlocked) {
            if(!isBlockCached) {
                userModel.setSingleAttribute(
                        UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_BLOCKED_AT,
                        String.valueOf(Instant.now().plus(blockCacheExpire).toEpochMilli())
                );
            }
            failure(context, "Временная блокировка");
            return;
        } else {
            userModel.removeAttribute(UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_BLOCKED_AT);
        }

        if(userCode == null || userCode.isEmpty()) {
            if(codeSentAt != null && codeSentAt.isAfter(Instant.now().minus(newSendDelay))
                    && (codeHashKey == null || codeHashKey.isEmpty())) {
                failure(context, "Слишком много запросов отправки кода. Повторите позже");
                return;
            }
            if(codeSentAt == null
                    || codeSentAt.isBefore(Instant.now().minus(Duration.ofSeconds(codeType.getExpiredSecondsToResend())))
                    || codeHashKey == null
                    || codeHashKey.isEmpty()) {
                if(codeHashKey != null && !codeHashKey.isEmpty()) {
                    // Сохраняем попытку переотправки кода, см. resend input у формы
                    attemptFailsService.saveAttempt(new AttemptFailsDto(user.getPhone(), codeHashKey,
                            context.getRealm().getName(), codeType.name(), LocalDateTime.now(),
                            context.getUser().getId()));
                }
                String code;
                try {
                    if (codeType == ActivationCodeType.CODE_TO_SMS) {
                        code = SmsCodeGenerator.getCode(codeType.getLengthCode());
                        String[] messengers = context.getRealm().getSmtpConfig().get(MESSENGER).split(",");
                        messageSendService.sendMessageToMessengers(
                                user.getPhone(),
                                code, context.getRealm().getId(),
                                messengers,
                                host
                        );
                    } else {
                        code = phoneCallerService.callAndGetCode(user.getPhone(), 1);
                    }
                } catch (Exception e) {
                    log.info(e.getMessage(), e);
                    failure(context, "Ошибка отправки кода");
                    return;
                }
                codeSentAt = Instant.now();
                userModel.setSingleAttribute(UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_CODE_HASH_KEY,
                        HashGenerator.getSecretHash(code));
                userModel.setSingleAttribute(UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_CODE_SENT_AT,
                        String.valueOf(codeSentAt.toEpochMilli()));
                log.info("Code sent");
            }
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Код отправлен");
            result.put("expiration_seconds", Duration.between(
                    Instant.now(),
                    codeSentAt.plus(Duration.ofSeconds(codeType.getExpiredSecondsToResend()))
            ).getSeconds());
            context.challenge(Response.ok().entity(result).type(MediaType.APPLICATION_JSON_TYPE).build());
            return;
        }

        if(codeHashKey == null || codeHashKey.isEmpty() || codeSentAt == null) {
            failure(context, "Код не запрошен");
            return;
        }
        int codeAttempts = wroteCodeAttemptsService.getWroteCodeAttemptsByCode(user.getPhone(),
                context.getRealm().getName(), authType.name(), codeHashKey);
        if (codeAttempts >= countByOnCode) {
            failure(context, "Исчерпаны попытки ввода кода");
            userModel.removeAttribute(UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_CODE_SENT_AT);
            return;
        }
        session.setAuthNote(EXPIRATION_TIME, codeSentAt.atZone(ZoneOffset.systemDefault()).toLocalDateTime()
                .format(DateTimeFormatter.ISO_DATE_TIME));
        try {
            userPhoneVerifier.verifyPhone(userModel, user, codeType.getExpiredCodeSeconds(), codeHashKey, userCode,
                    codeType, session.getRealm().getName(), session, authorisedUsersService, authType.getId());
            userModel.removeAttribute(UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_CODE_HASH_KEY);
            context.success();
        } catch (WrongSmsCode e) {
            log.warn("Wrong sms code");
            failure(context, "Неверный код");
            wroteCodeAttemptsService.saveFailWroteCode(codeHashKey, userCode, user.getPhone(),
                    context.getRealm().getName(), authType.name(), user);
        } catch (TimeExpiredException e) {
            log.warn("Expired time of code");
            failure(context, "Код устарел");
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) { }

    private void failure(AuthenticationFlowContext context, String error) {
        Map<String, Object> result = new HashMap<>();
        result.put(OAuth2Constants.ERROR, OAuthErrorException.INVALID_REQUEST);
        result.put(OAuth2Constants.ERROR_DESCRIPTION, error);
        context.failure(
                AuthenticationFlowError.INTERNAL_ERROR,
                Response.status(Response.Status.FORBIDDEN)
                        .entity(result)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .build()

        );
    }
}
