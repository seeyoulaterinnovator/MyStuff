package ru.alamics.sso.keycloak.registration;

import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import ru.alamics.sso.registration.TbapiService;
import ru.alamics.sso.registration.UserExtension;
import ru.alamics.sso.remote.tbapi.TbapiMockService;

import java.util.List;

public class UserModelExtenderFactory implements FormActionFactory {

    public static final String PROVIDER_ID = "registration-user-extension";

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };
    private TbapiService tbapiService;
    private UserExtension userExtension;

    public UserModelExtenderFactory() {
        tbapiService = new TbapiService(new TbapiMockService());
        userExtension = new UserExtension();
    }

    @Override
    public String getDisplayType() {
        return "Registration user extension";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Common help text";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public FormAction create(KeycloakSession session) {
        return new UserModelExtender(tbapiService, userExtension);
    }

    @Override
    public void init(Config.Scope config) {
        // nothing to do here
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // nothing to do here
    }

    @Override
    public void close() {
        // nothing to do here
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
