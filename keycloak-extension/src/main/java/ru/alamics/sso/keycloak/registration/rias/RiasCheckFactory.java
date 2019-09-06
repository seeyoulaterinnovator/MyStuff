package ru.alamics.sso.keycloak.registration.rias;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import ru.alamics.sso.registration.rias.RiasService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;

@Slf4j
public class RiasCheckFactory implements FormActionFactory {

    private static final String PROVIDER_ID = "rias_checker";

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };


    public RiasCheckFactory(){}

    @Override
    public String getDisplayType() {
        return "Registration Rias checker";
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
        return "Get help text";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }

    @Override
    public FormAction create(KeycloakSession session) {

        log.info("Creating RiasCheckProvider");
        RiasService riasService;
        try {
            InitialContext context = new InitialContext();

            riasService = (RiasService) context.lookup("java:global/domru-sso/" + RiasService.class.getSimpleName());
            log.info("Got riasService from context");
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }

        return new RiasCheckProvider(riasService);

    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
