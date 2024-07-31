package ru.alamics.sso.keycloak.auth.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static javax.ws.rs.core.Response.Status.TOO_MANY_REQUESTS;
import static ru.alamics.sso.registration.phone.UserPhoneVerifier.EXPIRATION_TIME;
import static ru.alamics.sso.registration.phone.UserPhoneVerifier.MESSENGER;

/**
 * За основу взят {@link ru.alamics.sso.keycloak.auth.form.new_auth.NewAbstractAuthMailPhoneForm}.
 * Требует flow:
 * <ul>
 *     <li>{@link ru.alamics.sso.keycloak.auth.form.new_auth.new_rest.auth.UserNameOrPhoneRestValidator}</li>
 *     <li>{@link RestSmsOrPhoneCallPasswordValidator}</li>
 *     <li>{@link RestSmsOrPhoneCallAuth}</li>
 *     <li>{@link RestRequiredActionsAuthenticator}</li>
 * </ul>
 */
@Slf4j
public class RestSmsOrPhoneCallAuth extends AbstractAuthenticator {
    private static final String AUTH_OR_REG_TYPE_PARAM = "viaPhoneOnly";

    private static final String SMS_CODE_PARAM = "smscode";

    private static final String SMS_CODE_ID_PARAM = "smsCodeId";

    private static final String MESSAGE_FIELD = "message";

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
        boolean phoneVerificationRequired = Boolean.TRUE.toString().equals(
                config.get(RestSmsOrPhoneCallAuthFactory.PHONE_VERIFICATION_REQUIRED.getName())
        );

        // Parameters
        String host = context.getHttpRequest().getUri().getBaseUri().getHost();
        MultivaluedMap<String, String> params = context.getHttpRequest().getDecodedFormParameters();
        AuthOrRegType authType = AuthOrRegType.findByReqActProviderName(params.getFirst(AUTH_OR_REG_TYPE_PARAM));
        ActivationCodeType codeType = authType == AuthOrRegType.SMS_CODE ? ActivationCodeType.CODE_TO_SMS :
                authType == AuthOrRegType.PHONE_CALL ? ActivationCodeType.CODE_BY_PHONE_NUMBER : null;
        String userCodeId = params.getFirst(SMS_CODE_ID_PARAM);
        String userCodeValue = params.getFirst(SMS_CODE_PARAM);
        boolean isPassword = params.containsKey(CredentialRepresentation.PASSWORD);

        // Attributes
        AttributeCode attributeCode = AttributeCode.fromString(
                userModel.getFirstAttribute(UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_CODE_ID_AND_HASH_KEY)
        );
        Instant codeSentAt = MiscUtil.parseInstant(userModel.getFirstAttribute(
                UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_CODE_SENT_AT));
        BlockTimeout blockTimeout = BlockTimeout.fromString(
                userModel.getFirstAttribute(UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_BLOCKED_AT)
        );

        if(codeType == null) {
            if(isPassword) {
                context.success();
            } else {
                failure(context, "Не указан вариант отправки кода");
            }
            return;
        }

        if(user.getPhone() == null) {
            failure(context, "У пользователя нет телефона");
            return;
        }

        if(phoneVerificationRequired && (
                userModel.getRequiredActions().contains(AuthOrRegType.SMS_CODE.getReqActProviderName())
                        || userModel.getRequiredActions().contains(AuthOrRegType.PHONE_CALL.getReqActProviderName())
        )) {
            failure(context, "Телефон еще не верифицирован");
            return;
        }

        if(blockTimeout == null || Instant.now().isAfter(blockTimeout.getCachedAt())) {
            boolean isBlocked = false;
            BlackListDto blackList = blackListService.getBlockedUser(user.getPhone(), context);
            if (blackList != null &&
                    blackListService.isUserBlockedAuthByPhoneCallAndCause(user.getPhone(), context, codeType.name())) {
                isBlocked = true;
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
                Instant unblockedAt = blackListService.getBlockedUserByCause(
                        user.getPhone(), context, codeType.name()
                ).getUnblockedAt().atZone(ZoneOffset.systemDefault()).toInstant();
                Instant cachedAt = Instant.now().plus(blockCacheExpire);
                blockTimeout = BlockTimeout.builder()
                        .cachedAt(cachedAt.isAfter(unblockedAt) ? unblockedAt : cachedAt)
                        .unblockedAt(unblockedAt)
                        .build();
            } else {
                blockTimeout = null;
            }
        }
        if(blockTimeout != null) {
            userModel.setSingleAttribute(UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_BLOCKED_AT, blockTimeout.toString());
            failure(context, "Временная блокировка");
            Map<String, Object> result = new HashMap<>();
            result.put(MESSAGE_FIELD, "Временная блокировка");
            result.put("blockSeconds", lastSeconds(Duration.between(Instant.now(), blockTimeout.unblockedAt)));
            result.put(SMS_CODE_ID_PARAM, attributeCode.getId());
            context.challenge(Response.status(TOO_MANY_REQUESTS)
                    .entity(result)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .build());
            return;
        } else {
            userModel.removeAttribute(UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_BLOCKED_AT);
        }

        if(userCodeValue == null || userCodeValue.isEmpty()) {
            if(codeSentAt != null && codeSentAt.isAfter(Instant.now().minus(newSendDelay)) && attributeCode == null) {
                failure(context, "Слишком много запросов отправки кода. Повторите позже");
                return;
            }
            if(codeSentAt == null
                    || codeSentAt.isBefore(Instant.now().minus(Duration.ofSeconds(codeType.getExpiredSecondsToResend())))
                    || attributeCode == null) {
                if(attributeCode != null) {
                    // Сохраняем попытку переотправки кода, см. resend input у формы
                    attemptFailsService.saveAttempt(new AttemptFailsDto(user.getPhone(), attributeCode.getHashKey(),
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
                attributeCode = AttributeCode.builder()
                        .id(UUID.randomUUID().toString())
                        .hashKey(HashGenerator.getSecretHash(code))
                        .build();
                userModel.setSingleAttribute(UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_CODE_ID_AND_HASH_KEY,
                        attributeCode.toString());
                userModel.setSingleAttribute(UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_CODE_SENT_AT,
                        String.valueOf(codeSentAt.toEpochMilli()));
                log.info("Code sent");
            }
            Map<String, Object> result = new HashMap<>();
            result.put(MESSAGE_FIELD, "Код отправлен");
            result.put("expirationSeconds", lastSeconds(Duration.between(
                    Instant.now(),
                    codeSentAt.plus(Duration.ofSeconds(codeType.getExpiredSecondsToResend()))
            )));
            result.put(SMS_CODE_ID_PARAM, attributeCode.getId());
            context.challenge(Response.ok().entity(result).type(MediaType.APPLICATION_JSON_TYPE).build());
            return;
        }

        if(attributeCode == null || codeSentAt == null) {
            failure(context, "Код не запрошен");
            return;
        }
        if(userCodeId == null || userCodeId.isEmpty()) {
            failure(context, "Не передан ID кода");
            return;
        }
        if(!userCodeId.equals(attributeCode.getId())) {
            failure(context, "Не активного кода с переданным идентификатором");
            return;
        }
        int codeAttempts = wroteCodeAttemptsService.getWroteCodeAttemptsByCode(user.getPhone(),
                context.getRealm().getName(), authType.name(), attributeCode.getHashKey());
        if (codeAttempts >= countByOnCode) {
            failure(context, "Исчерпаны попытки ввода кода");
            userModel.removeAttribute(UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_CODE_SENT_AT);
            return;
        }
        session.setAuthNote(EXPIRATION_TIME, codeSentAt.atZone(ZoneOffset.systemDefault()).toLocalDateTime()
                .format(DateTimeFormatter.ISO_DATE_TIME));
        try {
            userPhoneVerifier.verifyPhone(
                    userModel, user, codeType.getExpiredCodeSeconds(), attributeCode.getHashKey(), userCodeValue, codeType,
                    session.getRealm().getName(), session, authorisedUsersService, authType.getId()
            );
            userModel.removeAttribute(UserConstants.ATTR_REST_SMS_OR_PHONE_CALL_CODE_ID_AND_HASH_KEY);
            context.success();
        } catch (WrongSmsCode e) {
            log.warn("Wrong sms code");
            failure(context, "Неверный код");
            wroteCodeAttemptsService.saveFailWroteCode(attributeCode.getHashKey(), userCodeValue, user.getPhone(),
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

    private int lastSeconds(Duration duration) {
        return Math.max(0, (int) Math.ceil(duration.toMillis() / 1000.0));
    }

    @Data
    @AllArgsConstructor
    @Builder
    static class AttributeCode {
        static AttributeCode fromString(String value) {
            if(value == null || value.isEmpty()) {
                return null;
            } else {
                String[] words = value.split(":", 2);
                if(words.length != 2) return null;
                return new AttributeCode(words[0], words[1]);
            }
        }

        final String id;

        final String hashKey;

        @Override
        public String toString() {
            return id + ":" + hashKey;
        }
    }

    @Data
    @AllArgsConstructor
    @Builder
    static class BlockTimeout {
        static BlockTimeout fromString(String value) {
            if(value == null || value.isEmpty()) {
                return null;
            } else {
                String[] timestamps = value.split(":", 2);
                try {
                    return new BlockTimeout(
                            Instant.ofEpochMilli(Long.parseLong(timestamps[0])),
                            Instant.ofEpochMilli(Long.parseLong(timestamps[1]))
                    );
                } catch (Exception e) {
                    return null;
                }
            }
        }

        final Instant cachedAt;

        final Instant unblockedAt;

        @Override
        public String toString() {
            return cachedAt.toEpochMilli() + ":" + unblockedAt.toEpochMilli();
        }
    }
}
