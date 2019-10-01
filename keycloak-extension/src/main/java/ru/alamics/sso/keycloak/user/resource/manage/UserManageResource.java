package ru.alamics.sso.keycloak.user.resource.manage;

import org.keycloak.models.*;
import ru.alamics.sso.keycloak.response.JsonResponse;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

public class UserManageResource {

    private KeycloakSession session;
    private KeycloakContext context;
    private RealmModel realm;


    public UserManageResource (KeycloakSession session) {
        this.session = session;
        this.context = session.getContext();
        this.realm = context.getRealm();
    }


    @Path("/block")
    @POST
    public Response blockUsers(List<String> ids) {
        var userProvider = getUsers();
        if(ids != null) {
            ids.forEach(id -> {
                var user = userProvider.getUserById(id, realm);
                if(user != null) {
                    user.setEnabled(false);
                }
            });
        }
        return JsonResponse.success()
                .httpStatus(Response.Status.NO_CONTENT)
                .build();
    }

    @Path("/unlock")
    @POST
    public Response unlockUsers(List<String> ids) {
        var userProvider = getUsers();
        if(ids != null) {
            ids.forEach(id -> {
                var user = userProvider.getUserById(id, realm);
                if(user != null) {
                    user.setEnabled(true);
                }
            });
        }
        return JsonResponse.success()
                .httpStatus(Response.Status.NO_CONTENT)
                .build();
    }


    @Path("/credential/reset")
    @POST
    public Response resetPassword(List<String> ids) {
        var userProvider = getUsers();
        if(ids != null) {
            ids.forEach(id -> {
                var user = userProvider.getUserById(id, realm);
                user.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
            });
        }
        return JsonResponse.success()
                .httpStatus(Response.Status.NO_CONTENT)
                .build();
    }


    private UserProvider getUsers() {
        return this.session.users();
    }
}
