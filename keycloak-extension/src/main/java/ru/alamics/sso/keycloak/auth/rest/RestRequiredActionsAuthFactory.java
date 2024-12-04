package ru.alamics.sso.keycloak.auth.rest;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;

import static org.keycloak.models.AuthenticationExecutionModel.Requirement.*;

public class RestRequiredActionsAuthFactory extends AbstractAuthenticatorFactory {

    private static final String PROVIDER_ID = "rest-actions";
    public static final String DISPLAY_TYPE = "REST Actions";
    private static final String HELP_TEXT = "";
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {REQUIRED, CONDITIONAL, DISABLED};

    @Override
    public String getDisplayType() {
        return DISPLAY_TYPE;
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
        return new RestRequiredActionsAuthenticator(session);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}
