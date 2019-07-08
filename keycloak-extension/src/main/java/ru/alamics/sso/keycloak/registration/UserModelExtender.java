package ru.alamics.sso.keycloak.registration;

import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import ru.alamics.sso.registration.TbapiService;
import ru.alamics.sso.registration.UserExtension;

import java.util.Map;

public class UserModelExtender implements FormAction {

    private final TbapiService tbapiService;
    private final UserExtension userExtension;

    public UserModelExtender(TbapiService tbapiService, UserExtension userExtension) {
        this.tbapiService = tbapiService;
        this.userExtension = userExtension;
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        // nothing to do here
    }

    @Override
    public void validate(ValidationContext context) {
        context.success();
    }

    @Override
    public void success(FormContext context) {
        UserModel model = context.getUser();

        Map<String, String> attributes = tbapiService.registerUser(model);
        userExtension.extendUser(model, attributes);

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // nothing to do here
    }

    @Override
    public void close() {
        // nothing to do here
    }
}
