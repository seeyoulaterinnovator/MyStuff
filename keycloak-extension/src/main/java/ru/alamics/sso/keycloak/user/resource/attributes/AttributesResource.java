package ru.alamics.sso.keycloak.user.resource.attributes;

import lombok.Data;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.AttributeFormatException;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.user.UserAttributeService;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.web.AttributeRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
public class AttributesResource {

    private final UserAttributeService service;

    public AttributesResource(UserAttributeService service) {
        this.service = service;
    }

    @POST
    @Path("/{user_id}")
    public Response createAttributes(@PathParam("user_id") String userId, List<AttributeRequest> attributeRequests) {
        try {
            return JsonResponse.success()
                    .addResult("user", UserMapper.toDto(service.createAttributes(userId, attributeRequests)))
                    .build();
        } catch (FoundException e) {
            return JsonResponse.error(Response.Status.CONFLICT).message(e.getMessage()).build();
        } catch (AttributeFormatException e) {
            return JsonResponse.error(Response.Status.BAD_REQUEST).message(e.getMessage()).build();
        }
    }

    @PATCH
    @Path("/{user_id}")
    public Response patchAttributes(@PathParam("user_id") String userId, List<AttributeRequest> attributeRequests) {
        try {
            return JsonResponse.success()
                    .addResult("user", UserMapper.toDto(service.patchAttributes(userId, attributeRequests)))
                    .build();
        } catch (FoundException e) {
            return JsonResponse.error(Response.Status.CONFLICT).message(e.getMessage()).build();
        } catch (AttributeFormatException e) {
            return JsonResponse.error(Response.Status.BAD_REQUEST).message(e.getMessage()).build();
        }
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
