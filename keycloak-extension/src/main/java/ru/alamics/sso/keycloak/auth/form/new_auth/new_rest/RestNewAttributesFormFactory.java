package ru.alamics.sso.keycloak.auth.form.new_auth.new_rest;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class RestNewAttributesFormFactory implements RequiredActionFactory {

    private static final String DISPLAY_NAME = "New rest user selection post form";


    @Override
    public String getDisplayText() {
        return DISPLAY_NAME;
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return new RestNewAttributesForm();
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
        return RestNewAttributesForm.PROVIDER_ID;
    }
}
