package ru.alamics.sso.keycloak.auth.form.newForms;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import ru.alamics.sso.keycloak.auth.form.AbstractAuthMailPhoneForm;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.util.Util;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

@Slf4j
public class NewAuthMailPhoneForm extends NewAbstractAuthMailPhoneForm {


    public NewAuthMailPhoneForm(UserFindService userFindService) {
        super(userFindService);

    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return false;
    }

    @Override
    public boolean isSuccessCheckUser(AuthenticationFlowContext context, UserModel user) {
        return true;
    }
}