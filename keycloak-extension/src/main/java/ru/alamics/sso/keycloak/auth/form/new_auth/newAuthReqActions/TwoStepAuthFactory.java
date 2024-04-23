package ru.alamics.sso.keycloak.auth.form.new_auth.newAuthReqActions;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;
import ru.alamics.sso.keycloak.auth.model.AuthType;
import ru.alamics.sso.registration.model.UserConstants;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;
import java.util.stream.Collectors;

import static ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil.addRequiredAction;
import static ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil.log;

public class TwoStepAuthFactory extends AbstractAuthenticatorFactory implements Authenticator {

    private static final String PROVIDER_ID = "two-step-auth";

    private static final String HELP_TEXT = "Two step auth";

    private static final String DISPLAY_NAME = "Two step auth";

    private static final String NOTE_AUTH_TYPE_NAME = "note_auth_type_name";

    private static final String NOTE_AUTH_TYPE_DESC = "note_auth_type_DESC";

    private static final String TWO_STEP_VERIFICATION_TYPES = "two.step.verification.types";

    private static final String REFERENCE_CATEGORY = "two-step-verification-reference";

    public final static String CLIENT_B2B = "b2b";

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
        Map<String, String> config = context.getAuthenticatorConfig().getConfig();
        String type = config.get(TWO_STEP_VERIFICATION_TYPES);
        AuthType authType = AuthType.getByString(type);
        String disable = context.getUser().getFirstAttribute(UserConstants.DISABLE_TWO_STEP_AUTH);
        AuthenticationSessionModel sessionModel = context.getAuthenticationSession();
        UserModel userModel = context.getUser();

        if (context.getAuthenticationSession().getClient().getClientId().equals(CLIENT_B2B)) {
            addRequiredAction(context, "empty_req", userModel);
        }

        if (authType != null) {
            context.getAuthenticationSession().setAuthNote(NOTE_AUTH_TYPE_NAME, authType.name());
            context.getAuthenticationSession().setAuthNote(NOTE_AUTH_TYPE_DESC, authType.getDescription());
        }

        if (context.getAuthenticationSession().getAuthNote("loginPasswordButton") != null) {
            sessionModel.setAuthNote("loginPasswordButton", "loginPasswordButton");
            sessionModel.removeAuthNote("smsButton");
            sessionModel.removeAuthNote("phoneCallButton");

            if (authType != null && (disable == null || disable.isEmpty())) {
                for (String providerName : authType.getRequiredActionNames()) {
                    addRequiredAction(context, providerName, userModel);
                }
            }
            userModel.removeRequiredAction("rest_post_selector");
            addEmailReqActIfNeeded(userModel, context, "email_sender");
            userModel.removeRequiredAction("rest_post_selector");
            context.success();

        } else if (context.getAuthenticationSession().getAuthNote("smsButton") != null) {
            log.info("context.getAuthenticationSession().getAuthNote(\"smsButton\") != null");
            sessionModel.setAuthNote("smsButton", "smsButton");
            sessionModel.removeAuthNote("loginPasswordButton");
            sessionModel.removeAuthNote("phoneCallButton");

            if (authType != null && (disable == null || disable.isEmpty())) {
                for (String providerName : authType.getRequiredActionNames()) {
                    if (!providerName.equals("incoming_call_phone_verificator")) {
                        log.info("!providerName.equals(\"incoming_call_phone_verificator\"");
                        addRequiredAction(context, providerName, userModel);
                    }
                }
            }

            userModel.removeRequiredAction("incoming_call_phone_verificator");
            userModel.removeRequiredAction("phone_verificator_sms");
            userModel.removeRequiredAction("rest_post_selector");
            addEmailReqActIfNeeded(userModel, context, "email_sender");
            context.success();

        } else if (context.getAuthenticationSession().getAuthNote("phoneCallButton") != null) {
            sessionModel.setAuthNote("phoneCallButton", "phoneCallButton");
            sessionModel.removeAuthNote("loginPasswordButton");
            sessionModel.removeAuthNote("smsButton");

            if (authType != null && (disable == null || disable.isEmpty())) {
                for (String providerName : authType.getRequiredActionNames()) {
                    if (!providerName.equals("phone_verificator_sms")) {
                        addRequiredAction(context, providerName, userModel);
                    }
                }
            }

            userModel.removeRequiredAction("incoming_call_phone_verificator");
            userModel.removeRequiredAction("phone_verificator_sms");
            userModel.removeRequiredAction("rest_post_selector");
            addEmailReqActIfNeeded(userModel, context, "email_sender");
            context.success();
        }
    }

    private void addEmailReqActIfNeeded(UserModel user, AuthenticationFlowContext context, String providerName) {
        if (!user.isEmailVerified() && addRequiredAction(context, providerName, user)) {
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
