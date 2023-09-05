package ru.alamics.sso.keycloak.auth.form.new_auth.new_rest;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.jpa.entity.UserPostEntity;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.user.ImportReportService;
import ru.alamics.sso.user.UserService;
import ru.alamics.sso.user.UserServiceImpl;
import ru.alamics.sso.user.model.UserRequest;
import ru.alamics.sso.util.validator.NotValidException;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import javax.ws.rs.core.MediaType;
import java.util.List;


@Slf4j
public class CustomNewResource {

    private final UserService userService;
    private final AdminPermissionEvaluator auth;
    private final ImportReportService importReportService;
    private final RealmModel realm;

    protected KeycloakSession session;
    private final UserFindService userFindService;

    private final UserPostService userPostService;

    public CustomNewResource(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.auth = auth;
        auth.users().requireManage();
        this.userService = new UserServiceImpl(session, auth.adminAuth());
        this.importReportService = Lookup.lookup(ImportReportService.class);
        this.userFindService = Lookup.lookup(UserFindService.class);
        this.realm = session.getContext().getRealm();
        this.userPostService = Lookup.lookup(UserPostService.class);
    }

    @POST
    @Path("/findByQuery")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getUserByPhoneOrEmail(final UserRequest request, final HttpHeaders headers) {

        if (request.getPhone() != null) {
            UserEntity user = userFindService.getUserByPhone(session.getContext().getRealm(), request.getPhone());

            return user != null ?
                    JsonResponse.success()
                            .httpStatus(Response.Status.OK)
                            .addResult("id", user.getId())
                            .addResult("customerAccounts", userPostService.getUserPost(user.getId()))
                            .build()
                    :
                    JsonResponse.error(Response.Status.NOT_FOUND)
                            .message("phone not found").build();


        }

        if (request.getEmail() != null) {
            UserEntity user = userFindService.getFirstUserByEmail(session.getContext().getRealm().getId(), request.getEmail());

            return user != null ?
                    JsonResponse.success()
                            .addResult("id", user.getId())
                            .addResult("customerAccounts", userPostService.getUserPost(user.getId()))
                            .httpStatus(Response.Status.OK)
                            .build()
                    :
                    JsonResponse.error(Response.Status.NOT_FOUND)
                            .message("mail not found").build();
        }

        return JsonResponse.error(Response.Status.BAD_REQUEST)
                .build();
    }

    @GET
    @Path("/{sso_user_id}/customerAccounts")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCustomerAccountsByID(@PathParam("sso_user_id") String userId) {
        UserEntity user = userFindService.getUserEntity(userId);

        return user != null ?
                JsonResponse.success()
                        .httpStatus(Response.Status.OK)
                        .addResult("customerAccounts", userPostService.getUserPost(user.getId()))
                        .build()
                :
                JsonResponse.error(Response.Status.NOT_FOUND)
                        .message("user not found").build();
    }

    @DELETE
    @Path("/{sso_user_id}/customerAccounts/{customer_account_id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @NoCache
    public Response deleteCustomerById(@PathParam("sso_user_id") String userId, @PathParam("customer_account_id") String accountId) {
        UserEntity user = userFindService.getUserEntity(userId);
        List<UserPostEntity> userPostEntities = userPostService.getAllUserPostByUserId(user.getId());
        for (UserPostEntity upe : userPostEntities) {
            if (upe.getId().equals(accountId)) {
                userPostService.remove(accountId);
            }
        }
        return JsonResponse.success().build();
    }

    @GET
    @Path("/{sso_user_id}/dmpCustomerAccounts")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @NoCache
    public Response getDmpByUserID(@PathParam("sso_user_id") String userId) {
        UserEntity user = userFindService.getUserEntity(userId);

        return user != null ?
                JsonResponse.success()
                        .httpStatus(Response.Status.OK)
                        .addResult("customerAccounts", userPostService.getUserPost(user.getId()))
                        .build()
                :
                JsonResponse.error(Response.Status.NOT_FOUND)
                        .message("user not found").build();
    }

    @POST
    @Path("/{sso_user_id}/customerAccounts")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @NoCache
    public Response saveNewCustomer(@PathParam("sso_user_id") String userId, final UserPostRequest userPostRequest) {
        try {
            userPostRequest.setUserId(userId);
            userPostService.save(userPostRequest);
            return JsonResponse.success().build();
        } catch (NotFoundException | FoundUserPostException | NotValidException e) {
            log.error(e.getMessage());
            return ErrorResponse.error("invalid request", Response.Status.NOT_FOUND);
        }
    }

    @DELETE
    @Path("/{sso_user_id}/dmpCustomerAccounts/{dmp_customer_id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @NoCache
    public Response deleteCustomerByDmp(@PathParam("sso_user_id") String userId, @PathParam("dmp_customer_id") String dmpId) {
        UserEntity user = userFindService.getUserEntity(userId);
        List<UserPostEntity> userPostEntities = userPostService.getAllUserPostByUserId(user.getId());
        for (UserPostEntity upe : userPostEntities) {
            if (upe.getDmpId().equals(dmpId)) {
                userPostService.remove(upe.getId());
            }
        }
        return JsonResponse.success().build();
    }

    @POST
    @Path("/{sso_user_id}/dmpCustomerAccounts")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @NoCache
    public Response saveNewCustomerByDmp(@PathParam("sso_user_id") String userId, final UserPostRequest userPostRequest) {
        try {
            userPostRequest.setUserId(userId);
            userPostService.save(userPostRequest);
            return JsonResponse.success().build();
        } catch (NotFoundException | FoundUserPostException | NotValidException e) {
            log.error(e.getMessage());
            return ErrorResponse.error("invalid request", Response.Status.NOT_FOUND);
        }
    }
}
