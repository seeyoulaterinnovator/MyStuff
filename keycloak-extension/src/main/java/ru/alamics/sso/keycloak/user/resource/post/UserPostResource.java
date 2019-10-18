package ru.alamics.sso.keycloak.user.resource.post;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;
import ru.alamics.sso.keycloak.mapper.DataMapper;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.ExternalSystemRoleRequest;
import ru.alamics.sso.registration.dto.UserPostEditRequest;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.service.UserPostService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Slf4j
public class UserPostResource {

    protected KeycloakSession session;
    private UserPostService userPostService;

    public UserPostResource(KeycloakSession session) {
        this.session = session;
        try {
            this.userPostService = (UserPostService) new InitialContext().lookup("java:global/domru-sso/" + UserPostService.class.getSimpleName());
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response create(@NotNull @Valid UserPostRequest userPostRequest, HttpHeaders headers) {
        try {
            return JsonResponse.success()
                    .addResult("user_post", userPostService.save(userPostRequest))
                    .build();
        } catch (NotFoundException e) {
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/edit")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response edit(@Valid UserPostEditRequest userPostEditRequest, HttpHeaders headers) {
        try {
            return JsonResponse.success()
                    .addResult("user_post", userPostService.edit(userPostEditRequest))
                    .build();
        } catch (NotFoundException e) {
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/delete/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response delete(@PathParam("id") String id) {
        try {
            userPostService.remove(id);
            return JsonResponse.success()
                    .build();
        } catch (NotFoundException e) {
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response get(@PathParam("id") String id) {
        try {
            return JsonResponse.success()
                    .addResult("user-post", userPostService.get(id))
                    .build();
        } catch (NotFoundException e) {
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
    }


    @GET
    @Path("/users/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getUserPost(@PathParam("id") String userId) {
        try {
            return JsonResponse.success()
                    .addResult("user_post", DataMapper.getUserPostResponsesWithOrganizations(userPostService.getUserPost(userId)))
                    .build();
        } catch (NotFoundException e) {
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
    }


    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getAll() {
        return JsonResponse.success()
                .addResult("user-posts", userPostService.getAll())
                .build();
    }

    @GET
    @Path("/roles")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getAllRoles() {
        return JsonResponse.success()
                .addResult("roles", userPostService.getUserPostRoleDtos())
                .build();
    }

    @GET
    @Path("/system-roles")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getAllSystemRoles() {
        return JsonResponse.success()
                .addResult("system-roles", userPostService.getExternalSystemRoles())
                .build();
    }

    @GET
    @Path("/systems")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getAllSystems() {
        return JsonResponse.success()
                .addResult("systems", userPostService.getExternalSystems())
                .build();
    }

    @POST
    @Path("/add-system-role")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response addSystemRole(@NotNull @Valid ExternalSystemRoleRequest externalSystemRoleRequest) {
        try {
            return JsonResponse.success()
                    .addResult("user-post", userPostService.addSystemRole(externalSystemRoleRequest))
                    .build();
        } catch (NotFoundException e) {
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/remove-system-role")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response removeSystemRole(@NotNull @Valid ExternalSystemRoleRequest externalSystemRoleRequest) {
        try {
            return JsonResponse.success()
                    .addResult("user-post", userPostService.removeSystemRole(externalSystemRoleRequest))
                    .build();
        } catch (NotFoundException e) {
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
    }
}