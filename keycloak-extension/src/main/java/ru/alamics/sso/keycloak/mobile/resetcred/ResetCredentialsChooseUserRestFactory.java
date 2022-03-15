package ru.alamics.sso.keycloak.mobile.resetcred;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;

public class ResetCredentialsChooseUserRestFactory extends AbstractAuthenticatorFactory {

    public static final String DISPLAY_TEXT = "Найти пользователя Rest";
    public static final String HELP_TEXT = "Нахождение пользователя по email/phone для сброса пароля";
    public static final String PROVIDER_ID = "reset-cred-choose-user-rest";
    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    @Override
    public String getDisplayType() {
        return DISPLAY_TEXT;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new ResetCredentialsChooseUserRest();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
