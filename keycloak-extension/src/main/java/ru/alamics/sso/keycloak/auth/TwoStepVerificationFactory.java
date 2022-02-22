package ru.alamics.sso.keycloak.auth;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;
import ru.alamics.sso.keycloak.auth.model.AuthType;
import ru.alamics.sso.registration.model.UserConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class TwoStepVerificationFactory extends AbstractAuthenticatorFactory implements Authenticator {
    private static final String NOTE_AUTH_TYPE_NAME = "note_auth_type_name";
    private static final String NOTE_AUTH_TYPE_DESC = "note_auth_type_DESC";
    private static final String TWO_STEP_VERIFICATION_TYPES = "two.step.verification.types";

    private static final String PROVIDER_ID = "two-step-verification";
    private static final String DISPLAY_NAME = "Two step verification";
    private static final String HELP_TEXT = "Two step verification";
    private static final String REFERENCE_CATEGORY = "two-step-verification-reference";

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = Collections.singletonList(getTwoStepVerificationTypes());

    private static ProviderConfigProperty getTwoStepVerificationTypes() {
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(TWO_STEP_VERIFICATION_TYPES);
        property.setLabel("2-step verification types");
        property.setHelpText("");
        property.setOptions(Arrays.stream(AuthType.values()).map(Enum::name).collect(Collectors.toList()));
        property.setType(ProviderConfigProperty.LIST_TYPE);
        return property;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        Map<String, String> config = context.getAuthenticatorConfig().getConfig();
        String type = config.get(TWO_STEP_VERIFICATION_TYPES);
        AuthType authType = AuthType.getByString(type);

//        Залочили по скольку удаляет обязательные действия пользователя кроме конфига Two Steep
//        AuthType.REQUIRED_ACTIONS.forEach(x -> context.getUser().removeRequiredAction(x));

        String disable = context.getUser().getFirstAttribute(UserConstants.DISABLE_TWO_STEP_AUTH);
        if (authType != null && (disable == null || disable.isEmpty())) {
            for (String providerName : authType.getRequiredActionNames()) {
                context.getUser().addRequiredAction(providerName);
            }
        }
        if (authType != null) {
            context.getAuthenticationSession().setAuthNote(NOTE_AUTH_TYPE_NAME, authType.name());
            context.getAuthenticationSession().setAuthNote(NOTE_AUTH_TYPE_DESC, authType.getDescription());
        }

        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        context.success();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return this;
    }

    @Override
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
    }

    @Override
    public String getReferenceCategory() {
        return REFERENCE_CATEGORY;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

}
