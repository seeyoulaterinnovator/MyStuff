package ru.alamics.sso.keycloak.auth.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.oidc.OIDCLoginProtocolFactory;

public class RestOIDCLoginProtocolFactory extends OIDCLoginProtocolFactory {
    @Override
    public LoginProtocol create(KeycloakSession session) {
        return new RestOIDCLoginProtocol().setSession(session);
    }
}
