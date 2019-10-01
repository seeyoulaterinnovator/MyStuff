package ru.alamics.sso.keycloak.user.resource.manage;

import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

public class UserManageResourceProvider implements BaseResourceProvider<UserManageResource> {

    private final KeycloakSession session;

    public UserManageResourceProvider (KeycloakSession session) {
        this.session = session;
    }

    @Override
    public UserManageResource getResource () {
        this.initAuth(session);
        return new UserManageResource(session);
    }
}
