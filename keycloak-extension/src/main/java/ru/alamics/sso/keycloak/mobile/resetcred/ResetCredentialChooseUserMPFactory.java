package ru.alamics.sso.keycloak.mobile.resetcred;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;

public class ResetCredentialChooseUserMPFactory extends AbstractAuthenticatorFactory {

    public static final String PROVIDER_ID = "reset-cred-choose-user-mp";
    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    @Override
    public String getDisplayType() {
        return "Choose User MP";
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getHelpText() {
        return "Choose a user to reset credentials for";
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new ResetCredentialChooseUserMP();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
