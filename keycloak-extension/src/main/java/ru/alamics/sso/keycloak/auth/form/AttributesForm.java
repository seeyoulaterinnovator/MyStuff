package ru.alamics.sso.keycloak.auth.form;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.Response;

public class AttributesForm implements Authenticator {
    private static final String FORM = "attributes.ftl";


    @Override
    public void authenticate (AuthenticationFlowContext context) {
        Response challenge = context.form().createForm(FORM);
        context.challenge(challenge);
    }

    @Override
    public void action (AuthenticationFlowContext context) {

    }

    @Override
    public boolean requiresUser () {
        return false;
    }

    @Override
    public boolean configuredFor (KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions (KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close () {

    }
}
