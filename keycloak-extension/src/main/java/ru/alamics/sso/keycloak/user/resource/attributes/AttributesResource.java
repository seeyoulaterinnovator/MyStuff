package ru.alamics.sso.keycloak.user.resource.attributes;

import lombok.Data;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.web.AttributeRequest;
import ru.alamics.sso.user.UserService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
public class AttributesResource {

    private final UserService service;

    public AttributesResource (UserService service) {
        this.service = service;
    }

    @POST
    @Path("/{user_id}")
    public Response createAttributes(@PathParam("user_id") String userId, List<AttributeRequest> attributeRequests) {
        return JsonResponse.success()
                .addResult("user", UserMapper.toDto(service.createAttributes(userId, attributeRequests)))
                .build();
    }

    @PATCH
    @Path("/{user_id}")
    public Response patchAttributes(@PathParam("user_id") String userId, List<AttributeRequest> attributeRequests) {
        return JsonResponse.success()
                .addResult("user", UserMapper.toDto(service.patchAttributes(userId, attributeRequests)))
                .build();
    }

    @DELETE
    @Path("/{user_id}")
    public Response deleteAttributes(@PathParam("user_id") String userId, DeleteAttributesRequest attributeRequests) {
        return JsonResponse.success()
                .addResult("user", UserMapper.toDto(service.deleteAttributes(userId, attributeRequests.getAttributes())))
                .build();
    }



    @Data
    public static class DeleteAttributesRequest {
        private List<String> attributes;
    }


}
