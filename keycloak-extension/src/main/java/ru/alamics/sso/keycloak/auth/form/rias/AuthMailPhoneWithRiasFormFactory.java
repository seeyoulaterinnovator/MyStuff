package ru.alamics.sso.keycloak.auth.form.rias;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.DisplayTypeAuthenticatorFactory;
import org.keycloak.authentication.authenticators.console.ConsoleUsernamePasswordAuthenticator;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;
import ru.alamics.sso.registration.rias.RiasService;
import ru.alamics.sso.registration.service.UserFindService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import java.util.List;

@Slf4j
public class AuthMailPhoneWithRiasFormFactory implements AuthenticatorFactory, DisplayTypeAuthenticatorFactory {

    public static final String PROVIDER_ID = "auth-mail-phone-password-form";
    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };
    public static AuthMailPhoneWithRiasForm SINGLETON = null;

    @Override
    public Authenticator create(KeycloakSession session) {

        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();

        log.info("Get RiasAuthProvider");
        RiasService riasService;
        UserFindService userFindService;
        try {
            InitialContext context = new InitialContext();

            riasService = (RiasService) context.lookup("java:global/domru-sso/" + RiasService.class.getSimpleName());
            userFindService = (UserFindService) context.lookup("java:global/domru-sso/" + UserFindService.class.getSimpleName());
            log.info("Got riasService from context");
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }

        log.info("Creating AuthMailPhoneForm");
        SINGLETON = new AuthMailPhoneWithRiasForm(riasService, userFindService);

        return SINGLETON;
    }

    @Override
    public Authenticator createDisplay(KeycloakSession session, String displayType) {
        if (displayType == null) return SINGLETON;
        if (!OAuth2Constants.DISPLAY_CONSOLE.equalsIgnoreCase(displayType)) return null;
        return ConsoleUsernamePasswordAuthenticator.SINGLETON;
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

    @Override
    public String getReferenceCategory() {
        return UserCredentialModel.PASSWORD;
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
    public String getDisplayType() {
        return "(Phone or Mail) and Password Form";
    }

    @Override
    public String getHelpText() {
        return "Validates a username and password from login form. Username can be phone or mail";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }
}
