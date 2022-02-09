package ru.alamics.sso.keycloak.registration.sendEmail;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.FormAction;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import ru.alamics.sso.keycloak.registration.AbstractFormActionFactory;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class LetterSenderFactory extends AbstractFormActionFactory {

    private static final String PROVIDER_ID = "letter_sender";
    private static final String DISPLAY_NAME = "Send Registration Letter";
    private static final String HELP_TEXT = "Get help text";
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
    public List<ProviderConfigProperty> getConfigProperties() {
        return Arrays.asList();
    }

    @Override
    public FormAction create(KeycloakSession session) {
        return new LetterSenderProvider();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
