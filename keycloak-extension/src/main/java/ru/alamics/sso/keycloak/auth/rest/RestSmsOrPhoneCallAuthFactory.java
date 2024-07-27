package ru.alamics.sso.keycloak.auth.rest;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;
import ru.alamics.sso.keycloak.auth.form.new_auth.new_rest.auth.PasswordRestValidator;
import ru.alamics.sso.keycloak.auth.form.new_auth.new_rest.auth.UserNameOrPhoneRestValidator;

import java.util.Arrays;
import java.util.List;

public class RestSmsOrPhoneCallAuthFactory extends AbstractAuthenticatorFactory {
    public static final ProviderConfigProperty MAX_RESEND_RECALL_TRIES = new ProviderConfigProperty(
            "maxResendRecallTries",
            "Max resend recall tries",
            "Максимальное количество повторных попыток отправки кода перед временной блокировкой",
            ProviderConfigProperty.STRING_TYPE,
            4
    );

    public static final ProviderConfigProperty COUNT_BY_ONE_CODE = new ProviderConfigProperty(
            "countByOneCode",
            "Count by one code",
            "Максимальное количество попыток ввода кода перед временной блокировкой",
            ProviderConfigProperty.STRING_TYPE,
            5
    );

    public static final ProviderConfigProperty BLOCK_CHECK_CACHE_SECS = new ProviderConfigProperty(
            "blockCheckCacheSecs",
            "Block check cache seconds",
            "Интервал (в секундах) кеширования результата проверки временной блокировкой " +
                    "(для уменьшения количества обращений в БД)",
            ProviderConfigProperty.STRING_TYPE,
            15
    );

    public static final ProviderConfigProperty NEW_SEND_DELAY_SECS = new ProviderConfigProperty(
            "newSendDelaySecs",
            "New send delay seconds",
            "Интервал (в секундах) между успешными отправками-подтверждениями кодов (для защиты " +
                    "от автоматических попыток, а интервал отправки без подтверждения контролируется временем " +
                    "переотправки по умолчанию)",
            ProviderConfigProperty.STRING_TYPE,
            15
    );

    private static final String PROVIDER_ID = "rest-sms-or-phone-call-auth";

    private static final String DISPLAY_NAME = "REST SMS or phone call authentication";

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getHelpText() {
        return String.format(
                "Авторизация через отправку SMS кода или звонка по телефону для Direct Grant Flow " +
                        "(требует наличия перед собой \"%s\", \"%s\" и после себя \"%s\")",
                UserNameOrPhoneRestValidator.DISPLAY_NAME,
                PasswordRestValidator.DISPLAY_NAME,
                RestRequiredActionsAuthFactory.DISPLAY_TYPE
        );
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new RestSmsOrPhoneCallAuth();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Arrays.asList(
                MAX_RESEND_RECALL_TRIES,
                COUNT_BY_ONE_CODE,
                BLOCK_CHECK_CACHE_SECS,
                NEW_SEND_DELAY_SECS
        );
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }
}
