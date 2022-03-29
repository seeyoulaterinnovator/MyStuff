package ru.alamics.sso.keycloak.auth.requiredactions;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.DisplayTypeRequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.phone.ActivationCodeType;
import ru.alamics.sso.registration.phone.UserPhoneVerifier;

@Slf4j
public class PhoneVerificationByIncomingCallFactory extends AbstractRequiredActionFactory implements DisplayTypeRequiredActionFactory {

    public static final String PROVIDER_ID = "incoming_call_phone_verificator";
    private static final String DISPLAY_TEXT = "Phone Verification (incoming call)";

    public PhoneVerificationByIncomingCallFactory() {
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return createProvider(session);
    }

    @Override
    public RequiredActionProvider createDisplay(KeycloakSession session, String displayType) {
        if (displayType == null) return createProvider(session);
        return null;
    }

    private RequiredActionProvider createProvider(KeycloakSession session) {
        log.info("Creating provider for PhoneVerificationByIncomingCallFactory");
        UserPhoneVerifier userPhoneVerifier = Lookup.lookup(UserPhoneVerifier.class);
        log.info("Got userPhoneVerifier1 from context");
        EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);
        return new PhoneVerificationProvider(userPhoneVerifier, ActivationCodeType.CODE_BY_PHONE_NUMBER, emailTemplateProvider);
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