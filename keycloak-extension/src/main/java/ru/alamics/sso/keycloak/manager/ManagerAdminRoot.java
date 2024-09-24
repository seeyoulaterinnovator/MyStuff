package ru.alamics.sso.keycloak.manager;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import org.keycloak.services.resources.admin.AdminRoot;
import org.keycloak.services.resources.admin.RealmsAdminResource;

@Provider
@Path("/admin-manager")
public class ManagerAdminRoot extends AdminRoot {
    @Context
    ContainerRequestContext requestContext;

    @Override
    public RealmsAdminResource getRealmsAdmin() {
        RealmsAdminResource resource = super.getRealmsAdmin();

        if(!resource.getClass().equals(RealmsAdminResource.class)) return resource;

        Boolean replaceAuth = (Boolean) requestContext.getProperty(ManagerUserAdminRequestInterceptor.DISABLE_STRICT_AUTH_PROPERTY);

        if(replaceAuth == null || !replaceAuth) return resource;

        return new ManagerRealmsAdminResource(
                session,
                authenticateRealmAdminRequest(session.getContext().getRequestHeaders()),
                tokenManager
        );
    }
}
