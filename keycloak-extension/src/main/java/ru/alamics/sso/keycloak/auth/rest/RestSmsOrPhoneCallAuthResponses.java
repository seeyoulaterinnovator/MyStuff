package ru.alamics.sso.keycloak.auth.rest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuthErrorException;

import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@RequiredArgsConstructor
@Getter
public enum RestSmsOrPhoneCallAuthResponses {
    CODE_SENT(
            0,
            "restSmsOrPhoneCallAuthCodeSentMessage",
            "Код отправлен",
            Response.Status.OK
    ),
    INVALID_CODE_TYPE(
            1,
            "restSmsOrPhoneCallAuthInvalidCodeTypeMessage",
            "Не указан вариант отправки кода",
            Response.Status.BAD_REQUEST
    ),
    NO_PHONE(
            2,
            "restSmsOrPhoneCallAuthNoPhoneMessage",
            "У пользователя нет телефона",
            Response.Status.INTERNAL_SERVER_ERROR
    ),
    PHONE_NOT_VERIFIED(
            3,
            "restSmsOrPhoneCallAuthPhoneNotVerifiedMessage",
            "Телефон еще не верифицирован",
            Response.Status.BAD_REQUEST
    ),
    MANY_REQUESTS(
            4,
            "restSmsOrPhoneCallAuthManyRequestsMessage",
            "Слишком много запросов отправки кода. Повторите позже",
            Response.Status.TOO_MANY_REQUESTS
    ),
    SEND_FAILED(
            5,
            "restSmsOrPhoneCallAuthSendFailedMessage",
            "Ошибка отправки кода",
            Response.Status.INTERNAL_SERVER_ERROR
    ),
    NO_CODE_ID(
            7,
            "restSmsOrPhoneCallAuthNoCodeIdMessage",
            "Не передан ID кода",
            Response.Status.BAD_REQUEST
    ),
    NO_ACTIVE_CODE(
            8,
      "restSmsOrPhoneCallAuthNoActiveCodeMessage",
      "Не активного кода с переданным идентификатором",
            Response.Status.FORBIDDEN
    ),
    CODE_EXPIRED(
            11,
            "restSmsOrPhoneCallAuthCodeExpiredMessage",
            "Код устарел",
            Response.Status.FORBIDDEN
    ),
    WRONG_CODE(
            43,
            "restSmsOrPhoneCallAuthWrongCodeMessage",
            "Неверный код. Осталось {{attemptLeftLong}} ввода кода",
            Response.Status.FORBIDDEN
    ),
    CODE_ATTEMPT_EXHAUSTED(
            45,
            "restSmsOrPhoneCallAuthCodeAttemptExhaustedMessage",
            "Неверный код. Исчерпаны попытки ввода кода. " +
                    "Осталось {{resendLeftLong}} повторной отправки кода",
            Response.Status.FORBIDDEN
    ),
    NO_CODE(
            47,
            "restSmsOrPhoneCallAuthNoCodeMessage",
            "Код не запрошен",
            Response.Status.BAD_REQUEST
    ),
    BLOCKED(
            99,
            "restSmsOrPhoneCallAuthBlockedMessage",
            "Временная блокировка на {{blockSecondsLong}}",
            Response.Status.TOO_MANY_REQUESTS
    );

    final int errorCode;

    final String messageSetting;

    final String defaultMessage;

    final Response.Status status;

    String getErrorAlias() {
        switch (status) {
            case OK:
                return "";
            case INTERNAL_SERVER_ERROR:
                return OAuthErrorException.SERVER_ERROR;
            case FORBIDDEN:
                return OAuthErrorException.ACCESS_DENIED;
            default:
                return OAuthErrorException.INVALID_REQUEST;
        }
    }

    boolean isError() {
        return errorCode != 0;
    }

    public static void main(String[] args) throws Exception {
        StringBuilder builder = new StringBuilder();
        for(RestSmsOrPhoneCallAuthResponses response : RestSmsOrPhoneCallAuthResponses.values()) {
            builder.append(String.format("union all select '%s', '%s'%n", response.getMessageSetting(), response.getDefaultMessage()));
        }
        Files.write(Paths.get("temp.txt"), builder.toString().getBytes(StandardCharsets.UTF_8));
    }
}
