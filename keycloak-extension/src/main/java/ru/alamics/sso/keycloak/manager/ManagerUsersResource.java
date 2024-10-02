package ru.alamics.sso.keycloak.manager;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.resources.KeycloakOpenAPI;
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.USERS)
    @Operation( summary = "Create a new user Username must be unique.")
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

    @Path("{user-id}")
    public UserResource user(final @PathParam("user-id") String id) {
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
}
