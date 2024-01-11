package ru.alamics.sso.keycloak.auth.form.rias;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.DisplayTypeAuthenticatorFactory;
import org.keycloak.authentication.authenticators.console.ConsoleUsernamePasswordAuthenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserCredentialModel;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.rias.RiasService;
import ru.alamics.sso.registration.service.UserFindService;

@Slf4j
public class AuthMailPhoneWithRiasFormFactory extends AbstractAuthenticatorFactory implements DisplayTypeAuthenticatorFactory {

    private static final String PROVIDER_ID = "auth-mail-phone-pass-with-RIAS-form";
    private static final String DISPLAY_NAME = "(Phone or Mail) and Password Form with RIAS";
    private static final String HELP_TEXT = "Проверка логина и пароля для формы входа. Логин может быть телефоном или Email";
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    private static AuthMailPhoneWithRiasForm SINGLETON = null;

    @Override
    public Authenticator create(KeycloakSession session) {

        log.info("Got riasService from context");
        UserFindService userFindService = Lookup.lookup(UserFindService.class);
        log.info("Get RiasAuthProvider");
        RiasService riasService = Lookup.lookup(RiasService.class);

        log.info("Creating AuthMailPhoneForm");
        SINGLETON = new AuthMailPhoneWithRiasForm(riasService, userFindService);

        return SINGLETON;
    }

    @Override
    public Authenticator createDisplay(KeycloakSession session, String displayType) {
        if (displayType == null) return SINGLETON;
        if (!OAuth2Constants.DISPLAY_CONSOLE.equalsIgnoreCase(displayType)) return null;
        return ConsoleUsernamePasswordAuthenticator.SINGLETON;
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
