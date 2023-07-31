package ru.alamics.sso.keycloak.registration;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.forms.RegistrationUserCreation;

@Slf4j
public class RegistrationUserCreationExtension extends RegistrationUserCreation {

    public static final String PROVIDER_ID = "registration-user-creation-extension";

    @Override
    public void success(FormContext context) {
        super.success(context);

        context.getAuthenticationSession().setAuthNote("addRegisteredUser", "addRegisteredUser");

    }

    @Override
    public String getDisplayType() {
        return "Extended registration User Creation";
    }
}
