package ru.alamics.sso.keycloak.auth.form.new_auth;

import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.DisplayTypeAuthenticatorFactory;
import org.keycloak.authentication.authenticators.console.ConsoleUsernamePasswordAuthenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserCredentialModel;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.service.UserFindService;

public class NewAuthMailPhoneFormFactory extends AbstractAuthenticatorFactory implements DisplayTypeAuthenticatorFactory {

    private static final String PROVIDER_ID = "auth-mail-phone-pass-formq";

    private static final String DISPLAY_NAME = "New two step auth";

    private static final String HELP_TEXT = "Login/password or phone with sms or phone code";

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    private static NewAuthMailPhoneForm SINGLETON = null;

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public Authenticator createDisplay(KeycloakSession session, String displayType) {
        if (displayType == null) return SINGLETON;
        if (!OAuth2Constants.DISPLAY_CONSOLE.equalsIgnoreCase(displayType)) return null;
        return ConsoleUsernamePasswordAuthenticator.SINGLETON;
    }

    @Override
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        UserFindService userFindService = Lookup.lookup(UserFindService.class);

        SINGLETON = new NewAuthMailPhoneForm(userFindService, session);

        return SINGLETON;
    }

    @Override
    public String getReferenceCategory() {
        return UserCredentialModel.PASSWORD;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }


}
