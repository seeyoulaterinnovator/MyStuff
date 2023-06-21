package ru.alamics.sso.keycloak.auth.form.new_auth.new_auth_post;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;
import ru.alamics.sso.keycloak.auth.post.AttributesForm;

public class NewAttributesFormFactory extends AbstractAuthenticatorFactory {

    private static final String PROVIDER_ID = "new-attributes-form";

    private static final String DISPLAY_NAME = "New user selection post form";

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
        String clientId = session.getContext().getClient().getClientId();
        if (clientId.equals("security-admin-console")) {
            return new AttributesForm();
        } else {
            return new NewAttributesForm();
        }
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
