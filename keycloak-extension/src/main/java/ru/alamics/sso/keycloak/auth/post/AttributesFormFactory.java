package ru.alamics.sso.keycloak.auth.post;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import ru.alamics.sso.auth.UserRole;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;


@Slf4j
public class AttributesFormFactory implements AuthenticatorFactory {
    public static final String PROVIDER_ID = "attributes-form";

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };
    @Override
    public String getDisplayType () {
        return "User selection post form";
    }

    @Override
    public String getReferenceCategory () {
        return null;
    }

    @Override
    public boolean isConfigurable () {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices () {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed () {
        return false;
    }

    @Override
    public String getHelpText () {
        return "";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties () {
        return null;
    }

    @Override
    public Authenticator create (KeycloakSession session) {
        UserRole role = null;
        try {
            role = (UserRole) new InitialContext().lookup("java:global/domru-sso/" + UserRole.class.getSimpleName());
        } catch (NamingException e) {
            log.error("Cannot find userRole bean, HELP!!");
        }
        return new AttributesForm(role);
    }

    @Override
    public void init (Config.Scope config) {

    }

    @Override
    public void postInit (KeycloakSessionFactory factory) {

    }

    @Override
    public void close () {

    }

    @Override
    public String getId () {
        return PROVIDER_ID;
    }
}
