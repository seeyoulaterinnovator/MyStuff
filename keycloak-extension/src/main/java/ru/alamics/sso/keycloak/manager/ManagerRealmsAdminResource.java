package ru.alamics.sso.keycloak.manager;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.RealmAdminResource;
import org.keycloak.services.resources.admin.RealmsAdminResource;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;
import ru.alamics.sso.keycloak.GeneralRealm;
import ru.alamics.sso.keycloak.exception.RealmNotFoundException;

/**
 * Контроллер с переопределенной ослабленной авторизацией, как в Keycloak 6
 */
public class ManagerRealmsAdminResource extends RealmsAdminResource {
    final ContainerRequestContext requestContext;

    public ManagerRealmsAdminResource(
            KeycloakSession session,
            AdminAuth auth,
            TokenManager tokenManager,
            ContainerRequestContext requestContext
    ) {
        super(session, auth, tokenManager);
        this.requestContext = requestContext;
    }

    @Path("{realm}")
    public RealmAdminResource getRealmAdmin(@PathParam("realm") @Parameter(description = "realm name (not id!)") final String name) {
        RealmModel realm = session.realms().getRealmByName(name);
        if (realm == null) throw new RealmNotFoundException();

        if (!auth.getRealm().getName().equals(Config.getAdminRealm())
                && !auth.getRealm().getName().equals(GeneralRealm.MANAGER)
                && !auth.getRealm().equals(realm)) {
            throw new ForbiddenException();
        }

        AdminPermissionEvaluator realmAuth = AdminPermissions.evaluator(session, realm, auth);

        String newRealmName = (String) requestContext.getProperty(ManagerRequestProperties.ADMIN_CONTEXT_REALM);
        if(newRealmName != null) {
            realm = session.getProvider(RealmProvider.class).getRealmByName(newRealmName);
            if (realm == null) throw new RealmNotFoundException();
        }

        session.getContext().setRealm(realm);

        AdminEventBuilder adminEvent = new AdminEventBuilder(realm, auth, session, clientConnection);

        return new ManagerRealmAdminResource(session, realmAuth, adminEvent);
    }
}
