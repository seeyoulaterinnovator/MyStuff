package ru.alamics.sso.keycloak.auth.form;

import org.keycloak.Config;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.DisplayTypeAuthenticatorFactory;
import org.keycloak.authentication.authenticators.console.ConsoleUsernamePasswordAuthenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;
import ru.alamics.sso.registration.service.UserFindService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;

public class AuthMailPhoneFormFactory implements AuthenticatorFactory, DisplayTypeAuthenticatorFactory {

    private static final String PROVIDER_ID = "auth-mail-phone-pass-form";
    private static final String DISPLAY_NAME = "(Phone or Mail) and Password Form";
    private static final String HELP_TEXT = "Проверка логина и пароля для формы входа. Логин может быть телефоном или Email";
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    private static AuthMailPhoneForm SINGLETON = null;

    @Override
    public Authenticator create(KeycloakSession keycloakSession) {
        UserFindService userFindService;
        try {
            userFindService = (UserFindService) new InitialContext().lookup("java:global/domru-sso/" + UserFindService.class.getSimpleName());
        } catch (NamingException e) {
            throw new RuntimeException("Something wrong with context");
        }

        SINGLETON = new AuthMailPhoneForm(userFindService);

        return SINGLETON;
    }

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
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
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public Authenticator createDisplay(KeycloakSession keycloakSession, String displayType) {
        if (displayType == null) return SINGLETON;
        if (!OAuth2Constants.DISPLAY_CONSOLE.equalsIgnoreCase(displayType)) return null;
        return ConsoleUsernamePasswordAuthenticator.SINGLETON;
    }

    @Override
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }


    @Override
    public void init(Config.Scope scope) {

    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
