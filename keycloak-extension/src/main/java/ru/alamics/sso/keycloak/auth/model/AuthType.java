package ru.alamics.sso.keycloak.auth.model;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.UserModel;
import ru.alamics.sso.keycloak.auth.requiredactions.PhoneVerificationByIncomingCallFactory;
import ru.alamics.sso.keycloak.auth.requiredactions.PhoneVerificationBySmsFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
        if (type == null || type.isEmpty()) return null;
        try {
            return AuthType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.info("AuthType.getByString " + e);
            return null;
        }
    }

    public static AuthType getByList(List<String> types) {
        if (types == null || types.isEmpty()) return null;
        try {
            List<AuthType> authTypeList = Arrays.asList(EMAIL, EMAIL_AND_PHONE_CODE, INCOMING_CALL, PHONE_CODE);
            List<String> authTypes = new LinkedList<>();
            authTypeList.stream().forEach(o -> authTypes.addAll(Arrays.asList(o.getRequiredActionNames())));
            List<String> filterTypes = types.stream().filter(o -> authTypes.contains(o)).collect(Collectors.toList());
            return authTypeList.stream()
                    .filter(authType -> {
                        List<String> requiredActionNames1 = Arrays.asList(authType.getRequiredActionNames());
                        return filterTypes.containsAll(requiredActionNames1) && filterTypes.size() == requiredActionNames1.size();
                    }).findFirst()
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            log.info("AuthType.getByString " + e);
            return null;
        }
    }

    public String getDescription() {
        return description;
    }

    public String[] getRequiredActionNames() {
        return requiredActionNames;
    }

    public static List<String> REQUIRED_ACTIONS = Arrays.asList(
            UserModel.RequiredAction.VERIFY_EMAIL.toString(),
            PhoneVerificationByIncomingCallFactory.PROVIDER_ID,
            PhoneVerificationBySmsFactory.PROVIDER_ID
    );
}