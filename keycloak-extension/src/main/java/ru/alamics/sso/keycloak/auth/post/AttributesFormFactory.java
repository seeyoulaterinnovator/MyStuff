package ru.alamics.sso.keycloak.auth.post;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;


@Slf4j
public class AttributesFormFactory extends AbstractAuthenticatorFactory {
    private static final String PROVIDER_ID = "attributes-form";
    private static final String DISPLAY_NAME = "User selection post form";
    private static final String HELP_TEXT = "";

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
        return HELP_TEXT;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new AttributesForm();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
