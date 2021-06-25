package ru.alamics.sso.keycloak.auth.rest;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.auth.AuthFactoryBaseClass;

import static org.keycloak.models.AuthenticationExecutionModel.Requirement.*;

public class RestRequiredActionsAuthFactory extends AuthFactoryBaseClass {

    public static final String ID = "rest-actions";
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {REQUIRED, OPTIONAL, DISABLED};
    private static final String HELP_TEXT = "";
    private static final String DISPLAY_TYPE = "REST Actions";

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
        RestRequiredActionsAuthenticator restRequiredActionsAuthenticator = new RestRequiredActionsAuthenticator(session);
        ResteasyProviderFactory.getInstance().injectProperties(restRequiredActionsAuthenticator);
        restRequiredActionsAuthenticator.init();
        return restRequiredActionsAuthenticator;
    }

    @Override
    public String getId() {
        return ID;
    }

}
