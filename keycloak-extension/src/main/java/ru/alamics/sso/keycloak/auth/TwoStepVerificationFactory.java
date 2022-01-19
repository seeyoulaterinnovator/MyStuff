package ru.alamics.sso.keycloak.auth;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;
import ru.alamics.sso.keycloak.auth.model.AuthType;
import ru.alamics.sso.registration.model.UserConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class TwoStepVerificationFactory implements Authenticator, AuthenticatorFactory {
    public static final String VERIFY_PHONE_FTL = "verifyPhone.ftl";

    public static final String NOTE_AUTH_TYPE_NAME = "note_auth_type_name";
    public static final String NOTE_AUTH_TYPE_DESC = "note_auth_type_DESC";

    public static final String TWO_STEP_VERIFICATION_TYPES = "two.step.verification.types";
    private static final String PROVIDER_ID = "two-step-verification";

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = Arrays.asList(getTwoStepVerificationTypes());

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

        AuthType.REQUIRED_ACTIONS.forEach(x -> context.getUser().removeRequiredAction(x));

        String disable = context.getUser().getFirstAttribute(UserConstants.DISABLE_TWO_STEP_AUTH);
        if (authType != null && (disable == null || disable.isEmpty())) {
            for (String providerName : authType.getRequiredActionNames()) {
                context.getUser().addRequiredAction(providerName);
            }
        }
        context.getAuthenticationSession().setAuthNote(NOTE_AUTH_TYPE_NAME, authType.name());
        context.getAuthenticationSession().setAuthNote(NOTE_AUTH_TYPE_DESC, authType.getDescription());

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
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public String getHelpText() {
        return "Two step verification";
    }

    @Override
    public String getDisplayType() {
        return "Two step verification";
    }

    @Override
    public String getReferenceCategory() {
        return "two-step-verification-reference";
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
    public boolean isUserSetupAllowed() {
        return false;
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

    @Override
    public void close() {
    }
}
