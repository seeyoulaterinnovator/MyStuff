package ru.alamics.sso.keycloak.auth.form.new_auth.new_auth_rias;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserCredentialModel;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.rias.RiasService;
import ru.alamics.sso.registration.service.UserFindService;

@Slf4j
public class NewAuthMailPhoneWithRiasFormFactory extends AbstractAuthenticatorFactory {

    private static final String PROVIDER_ID = "new-with-RIAS-formq";
    private static final String DISPLAY_NAME = "New two step auth with RIAS";
    private static final String HELP_TEXT = "Login/password or phone with sms or phone code with RIAS";
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    @Override
    public Authenticator create(KeycloakSession session) {

        log.info("Got riasService from context");
        UserFindService userFindService = Lookup.lookup(UserFindService.class);
        log.info("Get RiasAuthProvider");
        RiasService riasService = Lookup.lookup(RiasService.class);


        log.info("Creating AuthMailPhoneForm");

        return new NewAuthMailPhoneWithRiasForm(riasService, userFindService, session);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getReferenceCategory() {
        return UserCredentialModel.PASSWORD;
    }


    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
    }

    @Override
    public String getHelpText() {
        return HELP_TEXT;
    }

}
