package ru.alamics.sso.keycloak.user.resource.userinfo;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.auth.UserRole;
import ru.alamics.sso.keycloak.facade.UserPostFacade;
import ru.alamics.sso.keycloak.response.JsonResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Slf4j
@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
public class UserInfoResource {
    private final AdminPermissionEvaluator auth;
    private final UserRole userRole;
    private final UserPostFacade userPostFacade;

    public UserInfoResource(AdminPermissionEvaluator auth) {
        this.auth = auth;
        try {
            this.userPostFacade = (UserPostFacade) new InitialContext().lookup("java:global/domru-sso/" + UserPostFacade.class.getSimpleName());
            this.userRole = (UserRole) new InitialContext().lookup("java:global/domru-sso/" + UserRole.class.getSimpleName());
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
    }

    @PUT
    @Path("/post/select/{postId}")
    @NoCache
    public Response select(@PathParam("postId") String postId) {
        userRole.selectPostByUser(auth.adminAuth().getUser(), postId);
        return JsonResponse.success()
                .build();
    }

    @GET
    @Path("/post")
    public Response allUserPosts() throws NotFoundException {
        try {
            return JsonResponse.success()
                    .addResult("user_post", userPostFacade.findByUserId(auth.adminAuth().getUser().getId()))
                    .build();
        } catch (NotFoundException e) {
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
    }
}
