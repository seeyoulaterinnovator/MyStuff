package ru.alamics.sso.keycloak.manager;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import org.keycloak.Config;
import org.keycloak.models.RealmProvider;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminRoot;
import ru.alamics.sso.keycloak.consts.RealmNames;

@Provider
@Path("/admin-manager")
public class ManagerAdminRoot extends AdminRoot {
    @Context
    ContainerRequestContext requestContext;

    @Override
    protected AdminAuth authenticateRealmAdminRequest(HttpHeaders headers) {
        AdminAuth auth =  super.authenticateRealmAdminRequest(headers);
        String userRealm = (String) requestContext.getProperty(ManagerRequestInterceptor.USER_REALM_PROPERTY);
        if(auth != null
                && auth.getRealm().getName().equals(RealmNames.MANAGER)
                && !session.getContext().getRealm().getName().equals(Config.getAdminRealm())
                && userRealm != null
        ) {
            auth = new AdminAuth(
                    session.getProvider(RealmProvider.class).getRealmByName(userRealm),
                    auth.getToken(),
                    auth.getUser(),
                    auth.getClient()
            );
        }
        return auth;
    }
}
