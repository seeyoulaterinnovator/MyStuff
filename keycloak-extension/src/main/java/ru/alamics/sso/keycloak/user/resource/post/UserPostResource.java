package ru.alamics.sso.keycloak.user.resource.post;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.auth.UserRole;
import ru.alamics.sso.keycloak.facade.CachedUserPostFacade;
import ru.alamics.sso.keycloak.facade.UserPostFacade;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.dto.ExternalSystemRoleRequest;
import ru.alamics.sso.registration.dto.UserPostEditRequest;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.service.UserPostService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Slf4j
public class UserPostResource {

    private final UserRole userRole;
    private KeycloakSession session;
    private UserPostService userPostService;
    private CachedUserPostFacade cachedUserPostFacade;
    private UserPostFacade userPostFacade;
    private AdminPermissionEvaluator auth;

    // TODO кэш?

    public UserPostResource(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.auth = auth;
        try {
            this.userRole = (UserRole) new InitialContext().lookup("java:global/domru-sso/" + UserRole.class.getSimpleName());
            this.userPostService = (UserPostService) new InitialContext().lookup("java:global/domru-sso/" + UserPostService.class.getSimpleName());
            this.cachedUserPostFacade = (CachedUserPostFacade) new InitialContext().lookup("java:global/domru-sso/" + CachedUserPostFacade.class.getSimpleName());
            this.userPostFacade = (UserPostFacade) new InitialContext().lookup("java:global/domru-sso/" + UserPostFacade.class.getSimpleName());
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
    }

    @PUT
    @Path("/select")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response select(@QueryParam("userId") String userId, @QueryParam("postId") String postId) {
        auth.users().requireManage();
        UserModel userModelById = session.users().getUserById(userId, session.getContext().getRealm());
        userRole.selectPostByUser(userModelById, postId);
        return JsonResponse.success()
                .build();
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response create(@NotNull @Valid UserPostRequest userPostRequest, HttpHeaders headers) {
        auth.users().requireManage();

        try {
            return JsonResponse.success()
                    .addResult("user_post", cachedUserPostFacade.save(userPostRequest))
                    .build();
        } catch (Exception e) {
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
        auth.users().requireManage();

        try {
            return JsonResponse.success()
                    .addResult("user_post", cachedUserPostFacade.edit(userPostEditRequest))
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
        auth.users().requireManage();

        try {
            cachedUserPostFacade.remove(id);
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
                    .addResult("user_post", userPostFacade.findByUserId(userId))
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
                .addResult("system-roles", userPostService.getAllExternalSystemRoleDTO())
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
        auth.users().requireManage();

        try {
            return JsonResponse.success()
                    .addResult("user-post", cachedUserPostFacade.addSystemRole(externalSystemRoleRequest))
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
        auth.users().requireManage();

        try {
            return JsonResponse.success()
                    .addResult("user-post", cachedUserPostFacade.removeSystemRole(externalSystemRoleRequest))
                    .build();
        } catch (NotFoundException e) {
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/clear-cache")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response clearCache() {
        auth.users().requireManage();

        cachedUserPostFacade.clearCache();
        return JsonResponse.success()
                .build();
    }

    @POST
    @Path("/clear-cache/{userId}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response clearCacheByUserId(@PathParam("userId") String userId) {
        auth.users().requireManage();

        cachedUserPostFacade.clearCacheByUserId(userId);
        return JsonResponse.success()
                .build();
    }
}