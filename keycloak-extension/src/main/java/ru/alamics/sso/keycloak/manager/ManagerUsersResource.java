package ru.alamics.sso.keycloak.manager;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.UserResource;
import org.keycloak.services.resources.admin.UsersResource;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.jpa.model.CustomUserAdapter;
import ru.alamics.sso.keycloak.GeneralRealm;
import ru.alamics.sso.keycloak.exception.UserNotFoundException;

public class ManagerUsersResource extends UsersResource {
    final AdminPermissionEvaluator auth;

    public ManagerUsersResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        super(session, auth, adminEvent);
        this.auth = auth;
    }

    @Override
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(UserRepresentation rep) {
        String authRealm = auth.adminAuth().getRealm().getName();
        String contextRealm = session.getContext().getRealm().getName();
        if (GeneralRealm.MANAGER_REALMS.contains(authRealm) && (
               contextRealm.equals(Config.getAdminRealm()) || GeneralRealm.MANAGER_REALMS.contains(contextRealm)
        )) {
            throw new ForbiddenException();
        }
        return super.createUser(rep);
    }

    @Override
    @Path("{user-id}")
    public UserResource user(final @PathParam("user-id") String id) {
        String authRealm = auth.adminAuth().getRealm().getName();

        UserModel user = session.getProvider(UserProvider.class)
                .getUserById(session.getContext().getRealm(), id);

        if (user == null) throw new UserNotFoundException();

        if(!(user instanceof CustomUserAdapter customUser)) throw new InternalServerErrorException();

        String userRealm = customUser.getRealm().getName();

        if(GeneralRealm.MANAGER_REALMS.contains(authRealm) && (
                userRealm.equals(Config.getAdminRealm()) || GeneralRealm.MANAGER_REALMS.contains(userRealm)
        )) {
            throw new ForbiddenException();
        }

        return super.user(id);
    }
}
