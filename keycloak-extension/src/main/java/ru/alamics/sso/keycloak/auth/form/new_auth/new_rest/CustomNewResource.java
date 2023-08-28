package ru.alamics.sso.keycloak.auth.form.new_auth.new_rest;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.user.ImportReportService;
import ru.alamics.sso.user.UserService;
import ru.alamics.sso.user.UserServiceImpl;
import ru.alamics.sso.user.model.UserParameter;
import ru.alamics.sso.user.model.UserRequest;

import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Slf4j
public class CustomNewResource {

    private final UserService userService;
    private final AdminPermissionEvaluator auth;
    private final ImportReportService importReportService;
    private final RealmModel realm;
    protected KeycloakSession session;

    private final UserFindService userFindService;

    public CustomNewResource(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.auth = auth;
        auth.users().requireManage();
        this.userService = new UserServiceImpl(session, auth.adminAuth());
        this.importReportService = Lookup.lookup(ImportReportService.class);
        this.userFindService = Lookup.lookup(UserFindService.class);
        this.realm = session.getContext().getRealm();
    }

    @POST
    @Path("/findByQuery")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getUserByPhoneOrEmail(final UserRequest request, final HttpHeaders headers) {
        return JsonResponse.success()
                .addResult("user", userFindService.getUserByPhone(session.getContext().getRealm(), request.getPhone()))
                .build();
    }

}
