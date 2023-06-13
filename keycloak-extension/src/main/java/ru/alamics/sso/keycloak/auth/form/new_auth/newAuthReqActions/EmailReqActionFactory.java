package ru.alamics.sso.keycloak.auth.form.new_auth.newAuthReqActions;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class EmailReqActionFactory implements RequiredActionFactory {
    private static final String DISPLAY_TEXT = "New Email Sender";

    @Override
    public RequiredActionProvider create(KeycloakSession keycloakSession) {
        return new EmailReqAction();
    }
    @Override
    public String getDisplayText() {
        return DISPLAY_TEXT;
    }

    @Override
    public void init(Config.Scope scope) {
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return EmailReqAction.PROVIDER_ID;
    }

}