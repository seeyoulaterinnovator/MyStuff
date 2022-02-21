package ru.alamics.sso.keycloak.create.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.Profile;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.*;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resources.account.AccountFormService;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.ClientsResource;
import org.keycloak.services.resources.admin.RoleMapperResource;
import org.keycloak.services.resources.admin.UsersResource;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.utils.ProfileHelper;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.model.UserEntityRepresentation;
import ru.alamics.sso.user.FileServiceException;
import ru.alamics.sso.user.ImportReportService;
import ru.alamics.sso.user.UserService;
import ru.alamics.sso.user.UserServiceImpl;
import ru.alamics.sso.user.filetype.FileModel;
import ru.alamics.sso.user.filetype.XlsxImpl;
import ru.alamics.sso.user.model.DownloadUserRequest;
import ru.alamics.sso.user.model.UserParameter;
import ru.alamics.sso.user.model.UserRequest;
import ru.alamics.sso.util.validator.NotValidException;

import javax.activation.UnsupportedDataTypeException;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.keycloak.models.ImpersonationSessionNote.IMPERSONATOR_ID;
import static org.keycloak.models.ImpersonationSessionNote.IMPERSONATOR_USERNAME;

@Slf4j
public class CustomUserResource {
    private final UserService userService;
    private final AdminPermissionEvaluator auth;
    private final ImportReportService importReportService;
    private final RealmModel realm;
    protected KeycloakSession session;

    public CustomUserResource(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.auth = auth;
        auth.users().requireManage();
        this.userService = new UserServiceImpl(session, auth.adminAuth());
        this.importReportService = Lookup.lookup(ImportReportService.class);

        this.realm = session.getContext().getRealm();
    }

    @POST
    @Path("")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(final UserRequest request, final HttpHeaders headers) {
        if (request.getPhone() == null || request.getPhone().isEmpty()) {
            return ErrorResponse.error("Phone is required attribute.", Response.Status.BAD_REQUEST);
        }
        if (validateEmail(request.getEmail())) {
            return ErrorResponse.error("Поле Email невалидно", Response.Status.BAD_REQUEST);
        }
        return getUserResponse(request, false);
    }

    @POST
    @Path("/bss")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUserBss(final UserRequest request, final HttpHeaders headers) {
        if (request.getPhone() == null || request.getPhone().isEmpty()) {
            return ErrorResponse.error("Поле Телефон должно быть заполнено", Response.Status.BAD_REQUEST);
        }
        if (request.getTomsId() == null || request.getTomsId().isEmpty()) {
            return ErrorResponse.error("Поле TomsId должно быть заполнено", Response.Status.BAD_REQUEST);
        }
        if (request.getName() == null || request.getName().isEmpty()) {
            return ErrorResponse.error("Поле name должно быть заполнено", Response.Status.BAD_REQUEST);
        }
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return ErrorResponse.error("Поле Email должно быть заполнено", Response.Status.BAD_REQUEST);
        }
        if (validateEmail(request.getEmail())) {
            return ErrorResponse.error("Поле Email невалидно", Response.Status.BAD_REQUEST);
        }

        return getUserResponse(request, true);
    }

    private boolean validateEmail(String email) {
        return !Pattern.matches("^([\\w-+]+(?:\\.[\\w-+]+)*)@((?:[\\w-]+\\.)*\\w[\\w-]{0,66})\\.([a-z]{2,6}(?:\\.[a-z]{2})?)", email);
    }

    private Response getUserResponse(UserRequest request, boolean bss) {
        try {
            UserModel user = userService.createUser(request, bss);
            return JsonResponse.success()
                    .httpStatus(Response.Status.CREATED)
                    .addResult("user_id", user.getId())
                    .build();
        } catch (ModelDuplicateException e) {
            log.error("Could not create user", e);
            return JsonResponse.error(Response.Status.BAD_REQUEST)
                    .message("Уже существует УЗ с таким username или email или phone")
                    .build();
        } catch (ModelException me) {
            log.error("Could not create user", me);
            return JsonResponse.error(Response.Status.INTERNAL_SERVER_ERROR)
                    .message("Не удалось создать УЗ")
                    .build();
        } catch (NotFoundException | FoundUserPostException e) {
            log.error("Could not create user", e);
            return JsonResponse.error(Response.Status.BAD_REQUEST)
                    .message(e.getMessage())
                    .build();
        } catch (FoundException e) {
            log.error("Could not create user", e);
            return JsonResponse.error(Response.Status.BAD_REQUEST)
                    .message(e.getMessage())
                    .addResult("info", e.getResult())
                    .build();
        } catch (NotValidException e) {
            log.error("NotValidException", e);
            return JsonResponse.error(Response.Status.BAD_REQUEST)
                    .message(e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/user-parameters")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserParameters() {
        return JsonResponse.success()
                .addResult("user-parameters", UserParameter.values())
                .build();
    }

    @POST
    @Path("/uploadUsers")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @NoCache
    @Transactional(Transactional.TxType.NEVER)
    public Response uploadUsers(@MultipartForm FileDto file, @HeaderParam(HttpHeaders.CONTENT_DISPOSITION) String content) {

        if (file == null ||
                content == null || content.isEmpty()) {
            return JsonResponse.error(Response.Status.BAD_REQUEST).build();
        }
        try (InputStream bas = new ByteArrayInputStream(file.getFileData())) {
            return JsonResponse.success()
                    .addResult("import-report",
                            userService.importUsers(bas, content))
                    .build();
        } catch (UnsupportedDataTypeException | FileServiceException e) {
            log.error("Could not upload users", e);
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        } catch (IOException e) {
            log.error("Could not upload users", e);
            return JsonResponse.fail()
                    .message("Error reading file")
                    .build();
        }
    }

    @POST
    @Path("/downloadUsers")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response downloadUsers(@NotNull @Valid DownloadUserRequest downloadUserRequest) {
        try {
            log.info("Start download users");
            byte[] bytes = userService.exportUsers(downloadUserRequest);
            if (bytes == null) {
                log.warn("Users not found");
                return JsonResponse
                        .fail()
                        .message("Users not found")
                        .build();
            }
            Response.ResponseBuilder response = Response.ok(bytes);
            response.header("Content-Disposition", "attachment; filename=\"users_info." + downloadUserRequest.getType() + "\"");
            if (downloadUserRequest.getType().equals("xlsx")) {
                response.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            } else {
                response.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM + ";charset=UTF-8");
            }
            log.info("Download users success!", "filename = users_info." + downloadUserRequest.getType());
            return response.build();
        } catch (UnsupportedDataTypeException e) {
            log.error("Could not download users", e);
            return JsonResponse
                    .error(Response.Status.BAD_REQUEST)
                    .message(e.getMessage())
                    .build();
        } catch (IOException e) {
            log.error("Could not download users", e);
            return JsonResponse.fail()
                    .message("Error writing file")
                    .build();
        }
    }

    @GET
    @Path("/importUsersReports")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImportUsersReports() {
        return JsonResponse.success()
                .addResult("importUsersReports", importReportService.findImportUsersReportsByRealmId(session.getContext().getRealm().getName()))
                .build();
    }

    @POST
    @Path("/uploadImportUsersFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @NoCache
    public Response uploadImportUsersFile(@MultipartForm FileDto file, @HeaderParam(HttpHeaders.CONTENT_DISPOSITION) String content) {
        if (file == null || content == null || content.isEmpty()) {
            return JsonResponse.error(Response.Status.BAD_REQUEST).build();
        }
        try (InputStream bas = new ByteArrayInputStream(file.getFileData())) {
            userService.uploadImportUsersFile(bas, content);
            return JsonResponse.success()
                    .build();
        } catch (UnsupportedDataTypeException | FileServiceException e) {
            log.error("Could not upload users", e);
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        } catch (IOException e) {
            log.error("Could not upload users", e);
            return JsonResponse.fail()
                    .message("Error reading file")
                    .build();
        }
    }

    @POST
    @Path("/downloadImportUsersTemplate/{type}")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public Response downloadImportUsersTemplate(@PathParam("type") String type) {
        log.info("Download import users template");
        try {
            if (type.equalsIgnoreCase("xlsx")) {
                byte[] bytes = IOUtils.toByteArray(CustomUserResource.class.getResourceAsStream("/template/template.xlsx"));
                Response.ResponseBuilder response = Response.ok(bytes);
                response.header("Content-Disposition", "attachment; filename=\"template.xlsx" + "\"");
                response.header("filename", "template.xlsx");
                response.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
                return response.build();
            }

            byte[] bytes = IOUtils.toByteArray(CustomUserResource.class.getResourceAsStream("/template/template.csv"));
            Response.ResponseBuilder response = Response.ok(bytes);
            response.header("Content-Disposition", "attachment; filename=\"template.csv" + "\"");
            response.header("filename", "template.csv");
            response.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM + ";charset=UTF-8");
            return response.build();
        } catch (IOException e) {
            log.error("Could not download import users template", e);
            return JsonResponse.fail()
                    .message("Error writing file")
                    .build();
        }
    }

    @POST
    @Path("/downloadImportUsersReport/{id}")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public Response downloadImportUsersReport(@PathParam("id") String importId) {
        try {
            log.info("Start download users");
            FileModel file = userService.downloadUsersByImportReportId(importId);
            Response.ResponseBuilder response = Response.ok(file.save());
            if (file instanceof XlsxImpl) {
                response.header("Content-Disposition", "attachment; filename=\"import_users_report.xlsx" + "\"");
                response.header("filename", "import_users_report.xlsx");
                response.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            } else {
                response.header("Content-Disposition", "attachment; filename=\"import_users_report.csv" + "\"");
                response.header("filename", "import_users_report.csv");
                response.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM + ";charset=UTF-8");
            }
            return response.build();
        } catch (UnsupportedDataTypeException e) {
            log.error("Could not download users", e);
            return JsonResponse
                    .error(Response.Status.BAD_REQUEST)
                    .message(e.getMessage())
                    .build();
        } catch (IOException e) {
            log.error("Could not download users", e);
            return JsonResponse.fail()
                    .message("Error writing file")
                    .build();
        }
    }

    @POST
    @Path("/activateImportUsersReport/{id}")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public Response activateImportUsersFromReport(@PathParam("id") String importId) {
        log.info("Start activate Import Users From Report:{}", importId);
        userService.activateImportUsersFromReport(importId);
        log.info("End activate Import Users From Report:{}", importId);
        return JsonResponse.success()
                .build();
    }

    @Path("impersonation/{id}")
    @POST
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> impersonate(@PathParam("id") String id) {
        session.userCache().clear();
        ProfileHelper.requireFeature(Profile.Feature.IMPERSONATION);

        UserModel user = session.users().getUserById(id, realm);
        auth.users().requireImpersonate(user);
        // if same realm logout before impersonation
        RealmModel authenticatedRealm = auth.adminAuth().getRealm();
        boolean sameRealm = false;
        ClientConnection clientConnection = session.getContext().getConnection();
        if (authenticatedRealm.getId().equals(realm.getId())) {
            sameRealm = true;
            UserSessionModel userSession = session.sessions().getUserSession(realm, auth.adminAuth().getToken().getSessionState());
            AuthenticationManager.expireIdentityCookie(realm, session.getContext().getUri(), clientConnection);
            AuthenticationManager.expireRememberMeCookie(realm, session.getContext().getUri(), clientConnection);
            AuthenticationManager.backchannelLogout(session, realm, userSession, session.getContext().getUri(), clientConnection, session.getContext().getRequestHeaders(), true);
        }
        EventBuilder event = new EventBuilder(realm, session, clientConnection);

        UserSessionModel userSession = session.sessions().createUserSession(realm, user, user.getUsername(), clientConnection.getRemoteAddr(), "impersonate", false, null, null);

        UserModel adminUser = auth.adminAuth().getUser();
        String impersonatorId = adminUser.getId();
        String impersonator = adminUser.getUsername();
        userSession.setNote(IMPERSONATOR_ID.toString(), impersonatorId);
        userSession.setNote(IMPERSONATOR_USERNAME.toString(), impersonator);

        AuthenticationManager.createLoginCookie(session, realm, userSession.getUser(), userSession, session.getContext().getUri(), clientConnection);
        URI redirect = AccountFormService.accountServiceApplicationPage(session.getContext().getUri()).build(realm.getName());
        Map<String, Object> result = new HashMap<>();
        result.put("sameRealm", sameRealm);
        result.put("redirect", redirect.toString());
        event.event(EventType.IMPERSONATE)
                .session(userSession)
                .user(user)
                .detail(Details.IMPERSONATOR_REALM, realm.getName())
                .detail(Details.IMPERSONATOR, impersonator).success();

        return result;
    }

    @Path("role-mappings/{id}")
    public RoleMapperResource getRoleMappings(@PathParam("id") String id) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        UserEntity userEntity = em.find(UserEntity.class, id);
        if (userEntity == null) throw new NotFoundException("User not found");
        UserModel user = new UserAdapter(session, realm, em, userEntity);

        AdminEventBuilder adminEvent = new AdminEventBuilder(realm, auth.adminAuth(), session, session.getContext().getConnection())
                .realm(realm)
                .resource(ResourceType.USER);

        AdminPermissionEvaluator.RequirePermissionCheck manageCheck = () -> auth.users().requireMapRoles(user);
        AdminPermissionEvaluator.RequirePermissionCheck viewCheck = () -> auth.users().requireView(user);
        RoleMapperResource resource = new RoleMapperResource(realm, auth, user, adminEvent, manageCheck, viewCheck);
        ResteasyProviderFactory.getInstance().injectProperties(resource);
        return resource;
    }

    @Path("clients")
    public ClientsResource getClients() {
        AdminEventBuilder adminEvent = new AdminEventBuilder(realm, auth.adminAuth(), session, session.getContext().getConnection())
                .realm(realm)
                .resource(ResourceType.REALM);
        ClientsResource clientsResource = new ClientsResource(realm, auth, adminEvent);
        ResteasyProviderFactory.getInstance().injectProperties(clientsResource);
        return clientsResource;
    }


    @Path("users")
    public UsersResource users() {
        session.userCache().clear();

        AdminEventBuilder adminEvent = new AdminEventBuilder(realm, auth.adminAuth(), session, session.getContext().getConnection())
                .realm(realm)
                .resource(ResourceType.REALM);
        UsersResource users = new UsersResource(realm, auth, adminEvent);
        ResteasyProviderFactory.getInstance().injectProperties(users);

        return users;
    }


    @Path("credential/reset-with-send-login")
    @POST
    public Response sendLoginAndResetPassword(List<String> ids) {
        sendLogin(ids, UserEntityRepresentation.SEND_LOGIN_AND_RESET_PASSWORD);
        return JsonResponse.success()
                .httpStatus(Response.Status.NO_CONTENT)
                .build();
    }

    @Path("/send/login")
    @POST
    public Response sendLogin(List<String> ids) {
        sendLogin(ids, UserEntityRepresentation.SEND_LOGIN);
        return JsonResponse.success()
                .httpStatus(Response.Status.NO_CONTENT)
                .build();
    }

    private void sendLogin(List<String> ids, final String requiredAction) {
        KeycloakContext context = session.getContext();
        AdminEventBuilder eventBuilder = new AdminEventBuilder(context.getRealm(), auth.adminAuth(), session, context.getConnection());
        eventBuilder.resource(ResourceType.USER);
        UserProvider userProvider = session.users();
        if (ids != null) {
            for (String id : ids) {
                UserModel user = userProvider.getUserById(id, realm);
                if (user != null) {
                    UserRepresentation rep = ModelToRepresentation.toRepresentation(session, realm, user);
                    rep.getRequiredActions().add(requiredAction);
                    eventBuilder.operation(OperationType.ACTION)
                            .resourcePath(session.getContext().getUri())
                            .representation(rep)
                            .realm(realm)
                            .success();
                }
            }
        }
    }

}
