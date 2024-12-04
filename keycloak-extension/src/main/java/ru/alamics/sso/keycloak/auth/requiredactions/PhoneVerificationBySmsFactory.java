package ru.alamics.sso.keycloak.auth.requiredactions;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.phone.ActivationCodeType;
import ru.alamics.sso.registration.phone.UserPhoneVerifier;

@Slf4j
public class PhoneVerificationBySmsFactory extends AbstractRequiredActionFactory {

    public static final String PROVIDER_ID = "phone_verificator_sms";
    private static final String DISPLAY_TEXT = "Phone Verification (sms)";

    public PhoneVerificationBySmsFactory() {
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return createProvider(session);
    }

    private RequiredActionProvider createProvider(KeycloakSession session) {
        log.info("Creating provider for PhoneVerificationBySmsFactory");
        UserPhoneVerifier userPhoneVerifier = Lookup.lookup(UserPhoneVerifier.class);
        log.info("Got userPhoneVerifier1 from context");

        return new PhoneVerificationProvider(userPhoneVerifier, ActivationCodeType.CODE_TO_SMS, session.getProvider(EmailTemplateProvider.class));
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }


    @Override
    public String getDisplayText() {
        return DISPLAY_TEXT;
    }

}
