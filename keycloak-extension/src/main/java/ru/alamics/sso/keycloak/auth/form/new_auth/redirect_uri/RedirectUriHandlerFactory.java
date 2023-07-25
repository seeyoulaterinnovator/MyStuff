package ru.alamics.sso.keycloak.auth.form.new_auth.redirect_uri;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;

public class RedirectUriHandlerFactory extends AbstractAuthenticatorFactory {

    private static final String PROVIDER_ID = "redirect_uri_handler";

    private static final String DISPLAY_NAME = "Redirect URI handler";

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
        return new RedirectUriHandlerProvider(session);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
