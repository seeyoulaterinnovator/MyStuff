package ru.alamics.sso.keycloak.create.rest;

import jakarta.activation.UnsupportedDataTypeException;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.reactive.NoCache;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;
import org.keycloak.Config;
import org.keycloak.admin.ui.rest.AvailableRoleMappingResource;
import org.keycloak.admin.ui.rest.EffectiveRoleMappingResource;
import org.keycloak.admin.ui.rest.model.ClientRole;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.Profile;
import org.keycloak.credential.CredentialModel;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.*;
import org.keycloak.models.cache.UserCache;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resources.account.AccountRestService;
import org.keycloak.services.resources.admin.*;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.utils.ProfileHelper;
import ru.alamics.sso.jpa.model.CustomUserAdapter;
import ru.alamics.sso.jpa.repository.BrandRepository;
import ru.alamics.sso.keycloak.GeneralRealm;
import ru.alamics.sso.keycloak.exception.UserNotFoundException;
import ru.alamics.sso.keycloak.facade.CustomerRequestService;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.keycloak.util.MiscUtil;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.model.UserEntityRepresentation;
import ru.alamics.sso.service.ValidateService;
import ru.alamics.sso.user.FileServiceException;
import ru.alamics.sso.user.ImportReportService;
import ru.alamics.sso.user.UserService;
import ru.alamics.sso.user.UserServiceImpl;
import ru.alamics.sso.user.filetype.FileModel;
import ru.alamics.sso.user.filetype.XlsxImpl;
import ru.alamics.sso.user.model.DownloadUserRequest;
import ru.alamics.sso.user.model.UserParameter;
import ru.alamics.sso.user.model.UserRequest;
import ru.alamics.sso.util.Util;
import ru.alamics.sso.util.validator.NotValidException;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.keycloak.models.ImpersonationSessionNote.IMPERSONATOR_ID;
import static org.keycloak.models.ImpersonationSessionNote.IMPERSONATOR_USERNAME;

@Slf4j
public class CustomUserResource {
    private final UserService userService;
    private final AdminPermissionEvaluator auth;
    private final ImportReportService importReportService;
    private final ValidateService validateService;
    private final RealmModel realm;
    protected KeycloakSession session;
    private CustomerRequestService customerRequestService;
    @Inject
    BrandRepository brandRepository;

    public CustomUserResource(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.auth = auth;
        auth.users().requireManage();
        this.userService = new UserServiceImpl(session, auth.adminAuth());
        this.importReportService = Lookup.lookup(ImportReportService.class);
        this.validateService = Lookup.lookup(ValidateService.class);
        this.customerRequestService = Lookup.lookup(CustomerRequestService.class);
        this.realm = session.getContext().getRealm();
    }

    @POST
    @Path("")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(final UserRequest request, final HttpHeaders headers) {
        if (Util.isEmpty(request.getPhone())) {
            return ErrorResponse.error("Поле Phone должно быть заполнено", Response.Status.BAD_REQUEST).getResponse();
        }
        if (!validatePhone(request.getPhone())) {
            return ErrorResponse.error("Поле Phone невалидно", Response.Status.BAD_REQUEST).getResponse();
        }
        if (Util.isEmpty(request.getEmail())) {
            return ErrorResponse.error("Поле Email должно быть заполнено", Response.Status.BAD_REQUEST).getResponse();
        }
        if (!validateEmail(request.getEmail())) {
            return ErrorResponse.error("Поле Email невалидно", Response.Status.BAD_REQUEST).getResponse();
        }
        return getUserResponse(request, false);
    }

    @POST
    @Path("/bss")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUserBss(final UserRequest request, final HttpHeaders headers) {
        if (Util.isEmpty(request.getPhone())) {
            return ErrorResponse.error("Поле Phone должно быть заполнено", Response.Status.BAD_REQUEST).getResponse();
        }
        if (!validatePhone(request.getPhone())) {
            return ErrorResponse.error("Поле Phone невалидно", Response.Status.BAD_REQUEST).getResponse();
        }
        if (Util.isEmpty(request.getTomsId())) {
            return ErrorResponse.error("Поле TomsId должно быть заполнено", Response.Status.BAD_REQUEST).getResponse();
        }
        if (Util.isEmpty(request.getName())) {
            return ErrorResponse.error("Поле name должно быть заполнено", Response.Status.BAD_REQUEST).getResponse();
        }
        if (Util.isEmpty(request.getEmail())) {
            return ErrorResponse.error("Поле Email должно быть заполнено", Response.Status.BAD_REQUEST).getResponse();
        }
        if (!validateEmail(request.getEmail())) {
            return ErrorResponse.error("Поле Email невалидно", Response.Status.BAD_REQUEST).getResponse();
        }

        return getUserResponse(request, true);
    }

    private boolean validateEmail(String email) {
        return Pattern.matches(Util.REGEX_EMAIL, email);
    }

    private boolean validatePhone(String phone) {
        return (phone.startsWith("+(7)9") && phone.length() == 14 && phone.substring(4).matches("[\\d]+"))
                || (phone.startsWith("7") && phone.length() == 11 && phone.matches("[\\d]+"));
    }

    private Response getUserResponse(UserRequest request, boolean bss) {
        try {
            UserModel user = userService.createUser(request, bss);
            customerRequestService.getCustomerName(request.getTomsId());
            return JsonResponse.success()
                    .httpStatus(Response.Status.CREATED)
                    .addResult("user_id", user.getId())
                    .build();
        } catch (ModelDuplicateException e) {
            log.error("Could not create user", e);
            customerRequestService.getCustomerName(request.getTomsId());
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
            customerRequestService.getCustomerName(request.getTomsId());
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
    public Response uploadUsers(MultipartFormDataInput input) {
        try {
            FormValue formValue = input.getValues().get("file").stream().findFirst().orElse(null);
            if (formValue != null) {
                return JsonResponse.success()
                        .addResult(
                                "import-report",
                                userService.importUsers(
                                        formValue.getFileItem().getInputStream(),
                                        formValue.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)
                                )
                        )
                        .build();
            } else {
                return JsonResponse.error(Response.Status.BAD_REQUEST).build();
            }
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
    public Response downloadUsers(DownloadUserRequest downloadUserRequest) {
        validateService.validate(downloadUserRequest);
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
            Response.ResponseBuilder response;
            if (downloadUserRequest.getType().equals("xlsx")) {
                response = Response.ok(bytes);
                response.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            } else {
                response = Response.ok(MiscUtil.addBom(bytes));
                response.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM + ";charset=UTF-8");
            }
            response.header("Content-Disposition", "attachment; filename=\"users_info." + downloadUserRequest.getType() + "\"");
            log.info("Download users success! filename = users_info." + downloadUserRequest.getType());
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
//    first - позиция начала передаваемых в ответе записей из результатов поиска
//    max - максимальное количество записей в ответе
    public Response getImportUsersReports(@QueryParam("first") int first, @QueryParam("max") int max) {
        String realmId = session.getContext().getRealm().getName();
        return JsonResponse.success()
                .addResult("importUsersReports", importReportService.findImportUsersReportsByRealmId(realmId, first, max))
                .addResult("nextUpdate", importReportService.getTimeNextUpdate(realmId))
                .build();
    }

    @POST
    @Path("/uploadImportUsersFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @NoCache
    public Response uploadImportUsersFile(MultipartFormDataInput input) {
        try {
            FormValue formValue = input.getValues().get("file").stream().findFirst().orElse(null);
            if (formValue != null) {
                userService.uploadImportUsersFile(
                        formValue.getFileItem().getInputStream(),
                        formValue.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)
                );
                String realmId = session.getContext().getRealm().getName();
                return JsonResponse.success()
                        .addResult("importUsersReports", importReportService.findImportUsersReportsByRealmId(realmId, 0, 11))
                        .addResult("nextUpdate", importReportService.getTimeNextUpdate(realmId))
                        .build();
            } else {
                return JsonResponse.error(Response.Status.BAD_REQUEST).build();
            }
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
                @Cleanup var stream = CustomUserResource.class.getResourceAsStream("/template/template.xlsx");
                byte[] bytes = IOUtils.toByteArray(stream);
                Response.ResponseBuilder response = Response.ok(bytes);
                response.header("Content-Disposition", "attachment; filename=\"template.xlsx" + "\"");
                response.header("filename", "template.xlsx");
                response.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
                return response.build();
            }
            @Cleanup var stream = CustomUserResource.class.getResourceAsStream("/template/template.csv");
            byte[] bytes = IOUtils.toByteArray(stream);
            Response.ResponseBuilder response = Response.ok(MiscUtil.addBom(bytes));
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
            byte[] bytes = file.save();
            Response.ResponseBuilder response;
            if (file instanceof XlsxImpl) {
                response = Response.ok(bytes);
                response.header("Content-Disposition", "attachment; filename=\"import_users_report.xlsx" + "\"");
                response.header("filename", "import_users_report.xlsx");
                response.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            } else {
                response = Response.ok(MiscUtil.addBom(bytes));
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
        clearUserCache(session);
        ProfileHelper.requireFeature(Profile.Feature.IMPERSONATION);

        CustomUserAdapter user;
        UserModel userModel = session.getProvider(UserProvider.class).getUserById(realm, id);
        if (userModel instanceof CustomUserAdapter) {
            user = (CustomUserAdapter) userModel;
        } else {
            throw new InternalServerErrorException();
        }
        RealmModel realm = user.getRealm();

        auth.users().requireImpersonate(user);
        // if same realm logout before impersonation
        RealmModel authenticatedRealm = auth.adminAuth().getRealm();
        boolean sameRealm = false;
        ClientConnection clientConnection = session.getContext().getConnection();
        if (authenticatedRealm.getId().equals(realm.getId())) {
            sameRealm = true;
            UserSessionModel userSession = session.sessions().getUserSession(realm, auth.adminAuth().getToken().getSessionState());
            AuthenticationManager.expireIdentityCookie(session);
            AuthenticationManager.expireRememberMeCookie(session);
            AuthenticationManager.backchannelLogout(session, auth.adminAuth().getRealm(), userSession, session.getContext().getUri(), clientConnection, session.getContext().getRequestHeaders(), true);
        }
        EventBuilder event = new EventBuilder(auth.adminAuth().getRealm(), session, clientConnection);

        UserSessionModel userSession = session.sessions().createUserSession(realm, user, user.getUsername(), clientConnection.getRemoteAddr(), "impersonate", false, null, null);

        UserModel adminUser = auth.adminAuth().getUser();
        String impersonatorId = adminUser.getId();
        String impersonator = adminUser.getUsername();
        userSession.setNote(IMPERSONATOR_ID.toString(), impersonatorId);
        userSession.setNote(IMPERSONATOR_USERNAME.toString(), impersonator);

        AuthenticationManager.createLoginCookie(session, realm, userSession.getUser(), userSession, session.getContext().getUri(), clientConnection);
        URI redirect = Urls.accountBase(session.getContext().getUri().getBaseUri())
                .path(AccountRestService.class, "applications")
                .build(realm.getName());
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
        UserModel user = session.getProvider(UserProvider.class).getUserById(realm, id);
        if (user == null) throw new UserNotFoundException();

        AdminEventBuilder adminEvent = new AdminEventBuilder(realm, auth.adminAuth(), session, session.getContext().getConnection())
                .realm(realm)
                .resource(ResourceType.USER);

        AdminPermissionEvaluator.RequirePermissionCheck manageCheck = () -> auth.users().requireMapRoles(user);
        AdminPermissionEvaluator.RequirePermissionCheck viewCheck = () -> auth.users().requireView(user);
        RoleMapperResource resource = new RoleMapperResource(session, auth, user, adminEvent, manageCheck, viewCheck);
        return resource;
    }

    @Path("ui-ext/effective-roles/users/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public List<ClientRole> listCompositeUsersRoleMappings(@PathParam("id") String id) {
        checkUser(id);
        return new EffectiveRoleMappingResource(session, realm, auth).listCompositeUsersRoleMappings(id);
    }

    @Path("ui-ext/available-roles/users/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public List<ClientRole> getEffectiveRoleMappings(
            @PathParam("id") String id, @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("10") int max, @QueryParam("search") @DefaultValue("") String search
    ) {
        checkUser(id);
        return new AvailableRoleMappingResource(session, realm, auth)
                .listAvailableUserRoleMappings(id, first, max, search);
    }

    @Path("clients")
    public ClientsResource getClients() {
        AdminEventBuilder adminEvent = new AdminEventBuilder(realm, auth.adminAuth(), session, session.getContext().getConnection())
                .realm(realm)
                .resource(ResourceType.REALM);
        ClientsResource clientsResource = new ClientsResource(session, auth, adminEvent);
        return clientsResource;
    }


    @Path("users")
    public UsersResource users() {
        clearUserCache(session);

        AdminEventBuilder adminEvent = new AdminEventBuilder(realm, auth.adminAuth(), session, session.getContext().getConnection())
                .realm(realm)
                .resource(ResourceType.REALM);

        return new UsersResource(session, auth, adminEvent);
    }


    @Path("credential/reset-with-send-login")
    @POST
    public Response sendLoginAndResetPassword(List<String> ids) {
        sendLogin(ids, UserEntityRepresentation.SEND_LOGIN_AND_RESET_PASSWORD);
        return JsonResponse.success()
                .httpStatus(Response.Status.NO_CONTENT)
                .build();
    }

    @Path("users/{userId}/credentials/{credentialId}")
    @DELETE
    @NoCache
    public Response removeCredential(final @PathParam("userId") String userId, final @PathParam("credentialId") String credentialId) {
        KeycloakContext context = session.getContext();
        AdminEventBuilder eventBuilder = new AdminEventBuilder(context.getRealm(), auth.adminAuth(), session, context.getConnection());
        eventBuilder.resource(ResourceType.USER);
        UserProvider userProvider = session.users();
        UserModel user = userProvider.getUserById(realm, userId);

        CredentialModel credential = user.credentialManager().getStoredCredentialById(credentialId);
        if (credential == null) {
            // we do this to make sure somebody can't phish ids
            if (auth.users().canQuery()) throw new NotFoundException("Credential not found");
            else throw new ForbiddenException();
        }
        user.credentialManager().removeStoredCredentialById(credentialId);
//        adminEvent.operation(OperationType.ACTION).resourcePath(session.getContext().getUri()).success();

        UserRepresentation rep = ModelToRepresentation.toRepresentation(session, realm, user);
        rep.getRequiredActions().add(UserEntityRepresentation.DELETE_PASSWORD);
        eventBuilder.operation(OperationType.ACTION)
                .resourcePath(session.getContext().getUri())
                .representation(rep)
                .realm(realm)
                .success();

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

    /**
     * see {@link RealmAdminResource#getRealm()}
     */
    @Path("realm/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public RealmRepresentation getUserRealm(@PathParam("id") String id) {
        checkUser(id);
        RealmRepresentation rep = new RealmRepresentation();
        rep.setRealm(realm.getName());
        rep.setDefaultLocale(realm.getDefaultLocale());
        rep.setDisplayName(realm.getDisplayName());
        rep.setDisplayNameHtml(realm.getDisplayNameHtml());
        rep.setSupportedLocales(realm.getSupportedLocalesStream().collect(Collectors.toSet()));
        rep.setRegistrationEmailAsUsername(realm.isRegistrationEmailAsUsername());
        RealmRepresentation r = ModelToRepresentation.toRepresentation(session, realm, false);
        rep.setIdentityProviders(r.getIdentityProviders());
        rep.setIdentityProviderMappers(r.getIdentityProviderMappers());
        return rep;
    }

    @Path("realm/{id}/groups")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Stream<GroupRepresentation> getUserRealmGroups(
            @PathParam("id") String id,
            @QueryParam("search") String search,
            @QueryParam("q") String searchQuery,
            @QueryParam("exact") @DefaultValue("false") Boolean exact,
            @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults,
            @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation,
            @QueryParam("populateHierarchy") @DefaultValue("true") boolean populateHierarchy
    ) {
        CustomUserAdapter user = checkUser(id);

        AdminEventBuilder adminEvent = new AdminEventBuilder(user.getRealm(), auth.adminAuth(), session, session.getContext().getConnection())
                .realm(user.getRealm())
                .resource(ResourceType.REALM);

        return new GroupsResource(user.getRealm(), session, auth, adminEvent)
                .getGroups(search, searchQuery, exact, firstResult, maxResults, briefRepresentation, populateHierarchy);
    }

    @Path("realm/{id}/groups/{groupId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public GroupResource getUserRealmGroupById(@PathParam("id") String id, @PathParam("id") String groupId) {
        CustomUserAdapter user = checkUser(id);

        AdminEventBuilder adminEvent = new AdminEventBuilder(user.getRealm(), auth.adminAuth(), session, session.getContext().getConnection())
                .realm(user.getRealm())
                .resource(ResourceType.REALM);

        return new GroupsResource(user.getRealm(), session, auth, adminEvent).getGroupById(groupId);
    }

    private void sendLogin(List<String> ids, final String requiredAction) {
        KeycloakContext context = session.getContext();
        AdminEventBuilder eventBuilder = new AdminEventBuilder(context.getRealm(), auth.adminAuth(), session, context.getConnection());
        eventBuilder.resource(ResourceType.USER);
        UserProvider userProvider = session.users();
        if (ids != null) {
            for (String id : ids) {
                UserModel user = userProvider.getUserById(realm, id);
                if (user != null) {
                    RealmModel realm = getUserRealmModel(id);
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

    @GET
    @Path("users/{id}/with-brand")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getUserWithBrand(@PathParam("id") String id) {
        UserModel user = session.users().getUserById(realm, id);
        if (user == null) throw new NotFoundException("User not found");

        Map<String, Object> rep = new HashMap<>();
        rep.put("id", user.getId());
        rep.put("username", user.getUsername());
        rep.put("email", user.getEmail());

        String brandId = user.getFirstAttribute("markBrandId");
        rep.put("markBrandId", brandId);

        if (brandId != null) {
            brandRepository.findById(brandId).ifPresent(brand -> rep.put("brand", brand.getName()));
        }

        return Response.ok(rep).build();
    }

    private void clearUserCache(KeycloakSession session) {
        UserCache cache = session.getProvider(UserCache.class);
        if (cache != null) {
            cache.clear();
        }
    }

    private CustomUserAdapter checkUser(String userId) {
        if (!auth.adminAuth().getRealm().getName().equals(Config.getAdminRealm())
                && !GeneralRealm.MANAGER_REALMS.contains(auth.adminAuth().getRealm().getName())) {
            throw new ForbiddenException();
        }

        UserModel user = session.getProvider(UserProvider.class).getUserById(session.getContext().getRealm(), userId);

        if (user == null) throw new UserNotFoundException();

        if (!(user instanceof CustomUserAdapter customUser)) throw new InternalServerErrorException();

        if (!auth.adminAuth().getRealm().getName().equals(Config.getAdminRealm())
                && customUser.getRealm().getName().equals(Config.getAdminRealm())) {
            throw new ForbiddenException();
        }
        return customUser;
    }

    private RealmModel getUserRealmModel(String userId) {
        UserModel user = session.getProvider(UserProvider.class).getUserById(session.getContext().getRealm(), userId);

        if (user instanceof CustomUserAdapter) return ((CustomUserAdapter) user).getRealm();

        return realm;
    }
}
