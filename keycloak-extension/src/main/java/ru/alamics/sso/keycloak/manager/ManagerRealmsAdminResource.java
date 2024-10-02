package ru.alamics.sso.keycloak.manager;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.*;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;
import ru.alamics.sso.jpa.model.CustomUserAdapter;
import ru.alamics.sso.keycloak.GeneralRealm;
import ru.alamics.sso.keycloak.exception.UserNotFoundException;

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
        RealmManager realmManager = new RealmManager(session);
        RealmModel realm = realmManager.getRealmByName(name);
        if (realm == null) throw new NotFoundException("Realm not found.");

        if (!auth.getRealm().getName().equals(Config.getAdminRealm())
                && !auth.getRealm().getName().equals(GeneralRealm.MANAGER)
                && !auth.getRealm().equals(realm)) {
            throw new ForbiddenException();
        }

        AdminPermissionEvaluator realmAuth = AdminPermissions.evaluator(session, realm, auth);

        String newRealmName = (String) requestContext.getProperty(ManagerRequestProperties.ADMIN_CONTEXT_REALM);
        if(newRealmName != null) {
            realm = realmManager.getRealmByName(newRealmName);
            if (realm == null) throw new NotFoundException("Realm not found.");
        }

        session.getContext().setRealm(realm);

        AdminEventBuilder adminEvent = new AdminEventBuilder(realm, auth, session, clientConnection);

        return new RealmAdminResource(session, realmAuth, adminEvent) {
            @Override
            public UsersResource users() {
                return new UsersResource(session, auth, adminEvent) {
                    @Override
                    public Response createUser(UserRepresentation rep) {
                        String authRealm = auth.adminAuth().getRealm().getName();
                        String contextRealm = session.getContext().getRealm().getName();
                        if (authRealm.equals(GeneralRealm.MANAGER) && (
                                contextRealm.equals(GeneralRealm.MANAGER) || contextRealm.equals(Config.getAdminRealm())
                        )) {
                            throw new ForbiddenException();
                        }
                        return super.createUser(rep);
                    }

                    @Override
                    public UserResource user(String id) {
                        String authRealm = auth.adminAuth().getRealm().getName();

                        UserModel user = session.getProvider(UserProvider.class)
                                .getUserById(session.getContext().getRealm(), id);

                        if (user == null) throw new UserNotFoundException();

                        if(!(user instanceof CustomUserAdapter customUser)) throw new InternalServerErrorException();

                        String userRealm = customUser.getRealm().getName();

                        if(authRealm.equals(GeneralRealm.MANAGER) && (
                                userRealm.equals(Config.getAdminRealm()) || userRealm.equals(GeneralRealm.MANAGER)
                        )) {
                            throw new ForbiddenException();
                        }

                        return super.user(id);
                    }
                };
            }
        };
    }
}
