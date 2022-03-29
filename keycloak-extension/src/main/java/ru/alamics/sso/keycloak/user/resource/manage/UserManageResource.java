package ru.alamics.sso.keycloak.user.resource.manage;

import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.*;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import ru.alamics.sso.keycloak.response.JsonResponse;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

public class UserManageResource {

    private final KeycloakSession session;
    private final RealmModel realm;
    private final AdminEventBuilder eventBuilder;


    UserManageResource(KeycloakSession session, AdminEventBuilder eventBuilder) {
        this.session = session;
        KeycloakContext context = session.getContext();
        this.eventBuilder = eventBuilder.resource(ResourceType.USER);
        this.realm = context.getRealm();
    }


    @Path("/block")
    @POST
    public Response blockUsers(List<String> ids) {
        changeUserBlockState(ids, false);
        return JsonResponse.success()
                .httpStatus(Response.Status.NO_CONTENT)
                .build();
    }

    @Path("/unlock")
    @POST
    public Response unlockUsers(List<String> ids) {
        changeUserBlockState(ids, true);
        return JsonResponse.success()
                .httpStatus(Response.Status.NO_CONTENT)
                .build();
    }

    @Path("/credential/reset")
    @POST
    public Response resetPassword(List<String> ids) {
        UserProvider userProvider = getUsers();
        if (ids != null) {
            ids.forEach(id -> {
                UserModel user = userProvider.getUserById(id, realm);
                user.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
            });
        }
        return JsonResponse.success()
                .httpStatus(Response.Status.NO_CONTENT)
                .build();
    }

    private void changeUserBlockState(List<String> ids, boolean unlocking) {
        UserProvider userProvider = getUsers();
        if (ids != null) {
            ids.forEach(id -> {
                UserModel user = userProvider.getUserById(id, realm);
                if (user != null) {
                    user.setEnabled(unlocking);
                    UserRepresentation rep = ModelToRepresentation.toRepresentation(session, realm, user);
                    eventBuilder.operation(OperationType.UPDATE)
                            .resourcePath(session.getContext().getUri())
                            .representation(rep)
                            .realm(realm)
                            .success();
                }
            });
        }
    }

    private UserProvider getUsers() {
        return this.session.users();
    }
}
