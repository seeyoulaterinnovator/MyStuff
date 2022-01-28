package ru.alamics.sso.keycloak.auth.form;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import ru.alamics.sso.registration.service.UserFindService;

@Slf4j
public class AuthMailPhoneForm extends AbstractAuthMailPhoneForm {

    public AuthMailPhoneForm(UserFindService userFindService) {
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
