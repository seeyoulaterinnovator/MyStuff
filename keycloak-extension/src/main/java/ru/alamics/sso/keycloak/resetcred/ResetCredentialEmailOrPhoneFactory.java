package ru.alamics.sso.keycloak.resetcred;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;

public class ResetCredentialEmailOrPhoneFactory extends AbstractAuthenticatorFactory {
    public static final String ID = "reset-credential-email-or-phone";
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {AuthenticationExecutionModel.Requirement.REQUIRED};
    private static final String HELP_TEXT = "";
    private static final String DISPLAY_TYPE = "Reset credential by email or phone(RIAS Enabled)";

    @Override
    public Authenticator create(KeycloakSession session) {
        return new ResetCredentialEmailOrPhone(session);
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
    public String getDisplayType() {
        return DISPLAY_TYPE;
    }

    @Override
    public String getId() {
        return ID;
    }
}
