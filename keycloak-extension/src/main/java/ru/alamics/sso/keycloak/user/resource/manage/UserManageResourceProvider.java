package ru.alamics.sso.keycloak.user.resource.manage;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

public class UserManageResourceProvider implements BaseResourceProvider<UserManageResource> {

    private final KeycloakSession session;

    public UserManageResourceProvider (KeycloakSession session) {
        this.session = session;
    }

    @Override
    public UserManageResource getResource () {
        var adminAuth = this.initAuth(session);
        var context = session.getContext();
        var adminEventBuilder = new AdminEventBuilder(context.getRealm(), adminAuth, session, context.getConnection());

        return new UserManageResource(session, adminEventBuilder);
    }
}
