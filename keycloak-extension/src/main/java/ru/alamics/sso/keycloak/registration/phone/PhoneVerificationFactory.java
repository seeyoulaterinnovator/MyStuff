package ru.alamics.sso.keycloak.registration.phone;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.authentication.DisplayTypeRequiredActionFactory;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.registration.phone.UserPhoneVerifier;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@Slf4j
public class PhoneVerificationFactory implements RequiredActionFactory, DisplayTypeRequiredActionFactory {

    private static final String PROVIDER_ID = "phone_verificator";

    private final UserModelUserMapper mapper;

    public PhoneVerificationFactory() {
        mapper = new UserModelUserMapper();
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return createProvider();
    }

    @Override
    public RequiredActionProvider createDisplay(KeycloakSession session, String displayType) {
        if (displayType == null) return createProvider();
        return null;
    }

    private RequiredActionProvider createProvider() {
        log.info("Creating PhoneVerificationProvider");
        UserPhoneVerifier userPhoneVerifier;
        try {
            InitialContext context = new InitialContext();

            userPhoneVerifier = (UserPhoneVerifier) context.lookup("java:global/domru-sso/" + UserPhoneVerifier.class.getSimpleName());
            log.info("Got userPhoneVerifier1 from context");
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }

        return new PhoneVerificationProvider(mapper, userPhoneVerifier);
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
        return "Phone Verification";
    }

    @Override
    public void close() {

    }

}
