package ru.alamics.sso.keycloak.auth.form.new_auth.new_auth_flow;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.phone.UserPhoneVerifier;

@Slf4j
public class SmsOrPhoneCallAuthFactory extends AbstractAuthenticatorFactory {
    private static final String PROVIDER_ID = "sms-or-phone";
    private static final String DISPLAY_NAME = "Sms or Phone Auth Action";
    private static final String HELP_TEXT = "";
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
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
        return createProvider(session);
    }

    private Authenticator createProvider(KeycloakSession session) {
        log.info("Creating provider for PhoneVerificationBySmsFactory");
        UserPhoneVerifier userPhoneVerifier = Lookup.lookup(UserPhoneVerifier.class);
        log.info("Got userPhoneVerifier1 from context");

        return new SmsOrPhoneCallAuth(userPhoneVerifier, session);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
