package ru.alamics.sso.keycloak.auth.model;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.UserModel;
import ru.alamics.sso.keycloak.auth.requiredactions.PhoneVerificationByIncomingCallFactory;
import ru.alamics.sso.keycloak.auth.requiredactions.PhoneVerificationBySmsFactory;

@Slf4j
public enum AuthType {
    EMAIL(
            new String[]{UserModel.RequiredAction.VERIFY_EMAIL.toString()},
            "На указанный адрес эл. почты будет выслана ссылка для подтверждения"
    ),
    EMAIL_AND_PHONE_CODE(
            new String[]{UserModel.RequiredAction.VERIFY_EMAIL.toString(), PhoneVerificationBySmsFactory.PROVIDER_ID},
            "На указанный email будет выслана ссылка, после прохождения по ней на указанный номер телефона в Viber или СМС придет код подтверждения"
    ),
    INCOMING_CALL(
            new String[]{PhoneVerificationByIncomingCallFactory.PROVIDER_ID},
            "На указанный Вами номер телефона будет совершен входящий звонок, последние 4 цифры номера - Ваш код доступа"
    ),
    PHONE_CODE(
            new String[]{PhoneVerificationBySmsFactory.PROVIDER_ID},
            "На указанный номер телефона будет выслано сообщение в Viber или СМС с одноразовым паролем"
    );

    private String description;
    private String[] requiredActionNames;

    AuthType(String requiredActionNames[], String description) {
        this.description = description;
        this.requiredActionNames = requiredActionNames;
    }

    public static AuthType getByString(String type) {
        if (type == null || type.isBlank()) return null;
        try {
            return AuthType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.info("AuthType.getByString " + e);
            return null;
        }
    }

    public String[] getRequiredActionNames() {
        return requiredActionNames;
    }
}