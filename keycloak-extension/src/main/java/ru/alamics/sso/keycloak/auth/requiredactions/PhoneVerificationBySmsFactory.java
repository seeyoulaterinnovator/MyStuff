package ru.alamics.sso.keycloak.auth.requiredactions;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.OAuth2Constants;
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
public class PhoneVerificationBySmsFactory implements RequiredActionFactory, DisplayTypeRequiredActionFactory {

    public static final String PROVIDER_ID = "phone_verificator_sms";

    public PhoneVerificationBySmsFactory() {}

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return createProvider(session);
    }

    @Override
    public RequiredActionProvider createDisplay(KeycloakSession session, String displayType) {
        if (displayType == null) return createProvider(session);
        if (!OAuth2Constants.DISPLAY_CONSOLE.equalsIgnoreCase(displayType)) return null;
        return null;
    }

    private RequiredActionProvider createProvider(KeycloakSession session) {
        log.info("Creating provider for PhoneVerificationBySmsFactory");
        UserPhoneVerifier userPhoneVerifier;
        try {
            InitialContext context = new InitialContext();

            userPhoneVerifier = (UserPhoneVerifier) context.lookup("java:global/domru-sso/" + UserPhoneVerifier.class.getSimpleName());
            log.info("Got userPhoneVerifier1 from context");
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }

        return new PhoneVerificationProvider(userPhoneVerifier, ActivationCodeType.CODE_TO_SMS, session.getProvider(EmailTemplateProvider.class));
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
        return "Phone Verification (sms)";
    }

    @Override
    public void close() {
    }
}