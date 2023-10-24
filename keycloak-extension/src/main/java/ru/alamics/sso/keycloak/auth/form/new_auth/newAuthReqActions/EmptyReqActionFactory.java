package ru.alamics.sso.keycloak.auth.form.new_auth.newAuthReqActions;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.service.RegisteredUsersService;

public class EmptyReqActionFactory implements RequiredActionFactory {

    private static final String DISPLAY_TEXT = "Empty Req";


    @Override
    public String getDisplayText() {
        return DISPLAY_TEXT;
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return new EmptyReq(Lookup.lookup(RegisteredUsersService.class));
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return EmptyReq.PROVIDER_ID;
    }
}
