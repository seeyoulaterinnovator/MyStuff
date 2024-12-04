package ru.alamics.sso.keycloak.auth.form;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserCredentialModel;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.service.UserFindService;

public class AuthMailPhoneFormFactory extends AbstractAuthenticatorFactory {

    private static final String PROVIDER_ID = "auth-mail-phone-pass-form";
    private static final String DISPLAY_NAME = "(Phone or Mail) and Password Form";
    private static final String HELP_TEXT = "Проверка логина и пароля для формы входа. Логин может быть телефоном или Email";
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    @Override
    public Authenticator create(KeycloakSession keycloakSession) {
        UserFindService userFindService = Lookup.lookup(UserFindService.class);

        return new AuthMailPhoneForm(userFindService);
    }

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
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
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
