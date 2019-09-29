package ru.alamics.sso.keycloak.auth;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public abstract class AuthBaseClass implements Authenticator {

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
