package ru.alamics.sso.keycloak.user.resource.manage;

import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

public class UserManageResourceProvider implements BaseResourceProvider<UserManageResource> {

    private final KeycloakSession session;

    public UserManageResourceProvider (KeycloakSession session) {
        this.session = session;
    }

    @Override
    public UserManageResource getResource () {
        AdminAuth adminAuth = this.initAuthByWorkingRealm(session);
        KeycloakContext context = session.getContext();
        AdminEventBuilder adminEventBuilder = new AdminEventBuilder(context.getRealm(), adminAuth, session, context.getConnection());

        return new UserManageResource(session, adminEventBuilder);
    }
}
