package ru.alamics.sso.keycloak.auth.requiredactions;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.authentication.DisplayTypeRequiredActionFactory;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import ru.alamics.sso.registration.phone.ActivationCodeType;
import ru.alamics.sso.registration.phone.UserPhoneVerifier;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@Slf4j
public class PhoneVerificationByIncomingCallFactory implements RequiredActionFactory, DisplayTypeRequiredActionFactory {

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
        UserPhoneVerifier userPhoneVerifier;
        try {
            InitialContext context = new InitialContext();

            userPhoneVerifier = (UserPhoneVerifier) context.lookup("java:global/domru-sso/" + UserPhoneVerifier.class.getSimpleName());
            log.info("Got userPhoneVerifier1 from context");
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
        EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);
        return new PhoneVerificationProvider(userPhoneVerifier, ActivationCodeType.CODE_BY_PHONE_NUMBER, emailTemplateProvider);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }


    @Override
    public String getDisplayText() {
        return DISPLAY_TEXT;
    }

    @Override
    public void close() {
    }
}