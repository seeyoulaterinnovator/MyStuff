package ru.alamics.sso.keycloak.manager;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.AdminRoot;
import org.keycloak.services.resources.admin.RealmsAdminResource;

/**
 * Контроллер с переопределенной ослабленной авторизацией, как в Keycloak 6
 */
@Provider
@Path("/admin-manager")
public class ManagerAdminRoot extends AdminRoot {
    @Context
    ContainerRequestContext requestContext;

    @Context
    KeycloakSession session;

    @Override
    public RealmsAdminResource getRealmsAdmin() {
        RealmsAdminResource resource = super.getRealmsAdmin();

        if(!resource.getClass().equals(RealmsAdminResource.class)) return resource;

        boolean disableStrictAdminAuth =
                Boolean.TRUE.equals(requestContext.getProperty(ManagerRequestProperties.DISABLE_STRICT_ADMIN_AUTH));

        if(!disableStrictAdminAuth) return resource;

        return new ManagerRealmsAdminResource(
                session,
                authenticateRealmAdminRequest(session.getContext().getRequestHeaders()),
                tokenManager,
                requestContext
        );
    }
}
