package ru.alamics.sso.keycloak.manager;

import jakarta.ws.rs.Path;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.RealmAdminResource;
import org.keycloak.services.resources.admin.UsersResource;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;

public class ManagerRealmAdminResource extends RealmAdminResource {
    final AdminEventBuilder adminEvent;

    public ManagerRealmAdminResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        super(session, auth, adminEvent);
        this.adminEvent = adminEvent;
    }

    @Path("users")
    public UsersResource users() {
        return new ManagerUsersResource(session, auth, adminEvent);
    }
}
