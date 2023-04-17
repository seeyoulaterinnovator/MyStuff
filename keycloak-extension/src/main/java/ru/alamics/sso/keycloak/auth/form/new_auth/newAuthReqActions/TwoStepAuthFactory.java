package ru.alamics.sso.keycloak.auth.form.new_auth.newAuthReqActions;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;
import ru.alamics.sso.keycloak.auth.model.AuthType;
import ru.alamics.sso.registration.model.UserConstants;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;
import java.util.stream.Collectors;

public class TwoStepAuthFactory extends AbstractAuthenticatorFactory implements Authenticator {

    private static final String PROVIDER_ID = "two-step-auth";

    private static final String HELP_TEXT = "Two step auth";

    private static final String DISPLAY_NAME = "Two step auth";

    private static final String NOTE_AUTH_TYPE_NAME = "note_auth_type_name";

    private static final String NOTE_AUTH_TYPE_DESC = "note_auth_type_DESC";

    private static final String TWO_STEP_VERIFICATION_TYPES = "two.step.verification.types";

    private static final String REFERENCE_CATEGORY = "two-step-verification-reference";


    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED,
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
        MultivaluedMap<String, String> buttons = context.getHttpRequest().getDecodedFormParameters();
        Map<String, String> config = context.getAuthenticatorConfig().getConfig();
        String type = config.get(TWO_STEP_VERIFICATION_TYPES);
        AuthType authType = AuthType.getByString(type);
        String disable = context.getUser().getFirstAttribute(UserConstants.DISABLE_TWO_STEP_AUTH);
        AuthenticationSessionModel sessionModel = context.getAuthenticationSession();
        UserModel userModel = context.getUser();

        if (authType != null) {
            context.getAuthenticationSession().setAuthNote(NOTE_AUTH_TYPE_NAME, authType.name());
            context.getAuthenticationSession().setAuthNote(NOTE_AUTH_TYPE_DESC, authType.getDescription());
        }

        if (buttons.containsKey("loginPasswordButton")) {
            sessionModel.setAuthNote("loginPasswordButton", "loginPasswordButton");
            sessionModel.removeAuthNote("smsButton");
            sessionModel.removeAuthNote("phoneCallButton");

            if (authType != null && (disable == null || disable.isEmpty())) {
                for (String providerName : authType.getRequiredActionNames()) {
                    userModel.addRequiredAction(providerName);
                }
            }
            addEmailReqActIfNeeded(userModel);
            context.success();

        } else if (buttons.containsKey("smsButton")) {
            sessionModel.setAuthNote("smsButton", "smsButton");
            sessionModel.removeAuthNote("loginPasswordButton");
            sessionModel.removeAuthNote("phoneCallButton");

            if (authType != null && (disable == null || disable.isEmpty())) {
                for (String providerName : authType.getRequiredActionNames()) {
                    if (!providerName.equals("incoming_call_phone_verificator")) {
                        userModel.addRequiredAction(providerName);
                    }
                }
            }

            userModel.removeRequiredAction("incoming_call_phone_verificator");
            userModel.removeRequiredAction("phone_verificator_sms");
            addEmailReqActIfNeeded(userModel);
            context.success();

        } else if (buttons.containsKey("phoneCallButton")) {
            sessionModel.setAuthNote("phoneCallButton", "phoneCallButton");
            sessionModel.removeAuthNote("loginPasswordButton");
            sessionModel.removeAuthNote("smsButton");

            if (authType != null && (disable == null || disable.isEmpty())) {
                for (String providerName : authType.getRequiredActionNames()) {
                    if (!providerName.equals("phone_verificator_sms")) {
                        userModel.addRequiredAction(providerName);
                    }
                }
            }

            userModel.removeRequiredAction("incoming_call_phone_verificator");
            userModel.removeRequiredAction("phone_verificator_sms");
            addEmailReqActIfNeeded(userModel);
            context.success();
        }
    }

    private void addEmailReqActIfNeeded(UserModel user) {
        if (!user.isEmailVerified()) {
            user.addRequiredAction("email_sender");
        }
    }

    @Override
    public String getReferenceCategory() {
        return REFERENCE_CATEGORY;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        context.success();
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

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
        return this;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void close() {
        super.close();
    }
}
