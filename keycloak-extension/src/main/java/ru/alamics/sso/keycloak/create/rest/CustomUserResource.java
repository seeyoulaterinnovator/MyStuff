package ru.alamics.sso.keycloak.create.rest;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.*;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ForbiddenException;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;
import ru.alamics.sso.keycloak.create.FileServiceException;
import ru.alamics.sso.keycloak.create.UserService;
import ru.alamics.sso.keycloak.create.model.DownloadUserRequest;
import ru.alamics.sso.keycloak.create.model.UserParameter;
import ru.alamics.sso.keycloak.create.model.UserRequest;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.FoundException;

import javax.activation.UnsupportedDataTypeException;
import javax.ws.rs.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;

@Slf4j
public class CustomUserResource {

    private final static Long DEFAULT_ROLE_ID = 1L;   //Соответствует роли LPR
    protected KeycloakSession session;
    private UserPostService userPostService;

    public CustomUserResource(KeycloakSession session) {
        try {
            this.userPostService = (UserPostService) new InitialContext().lookup("java:global/domru-sso/" + UserPostService.class.getSimpleName());
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
        this.session = session;
        AdminAuth auth = authenticateRealmAdminRequest(session.getContext().getRealm());
        this.userService = new UserService(session, auth);
    }

    @POST
    @Path("")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(final UserRequest request, final HttpHeaders headers) {
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            return ErrorResponse.error("Phone is required attribute", Response.Status.BAD_REQUEST);
        }

        RealmModel realm = session.getContext().getRealm();
        AdminAuth auth = authenticateRealmAdminRequest(realm);

        Response response = checkOnExistUser(request, realm);
        if (response != null) {
            return response;
        }

        return getUserResponse(request, realm, auth, true);
    }

    @POST
    @Path("/bss")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUserBss(final UserRequest request, final HttpHeaders headers) {
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            return ErrorResponse.error("Phone is required attribute", Response.Status.BAD_REQUEST);
        }
        if (request.getTomsId() == null || request.getTomsId().isBlank()) {
            return ErrorResponse.error("TomsId is required attribute", Response.Status.BAD_REQUEST);
        }

        RealmModel realm = session.getContext().getRealm();
        AdminAuth auth = authenticateRealmAdminRequest(realm);

        Response response = checkOnExistUser(request, realm);
        if (response != null) {
            return response;
        }

        return getUserResponse(request, realm, auth, false);
    }

    private Response checkOnExistUser(UserRequest request, RealmModel realm) {
        List<UserEntity> users = getEM().createQuery("select u from UserAttributeEntity atr join atr.user u " +
                "where atr.name = :ph_attr_name and " +
                " atr.value like '%' || :phone || '%' and" + // TODO =
                " u.realmId = :realId ", UserEntity.class)
                .setParameter("ph_attr_name", ATTR_PHONE_NAME)
                .setParameter("phone", request.getPhone())
                .setParameter("realId", realm.getId())
                .getResultList();

        if (users != null && !users.isEmpty()) {
            log.error("User exists with same phone {}", request.getPhone());
            return JsonResponse.error(Response.Status.CONFLICT)
                    .message("User exists with same phone")
                    .addResult("user_id", users.get(0).getId())
                    .build();
        }

        // Double-check duplicated username and email here due to federation
        UserModel userModel = session.users().getUserByUsername(request.getEmail(), realm);
        if (userModel != null) {
            log.error("User exists with same username {}", request.getEmail());
            return JsonResponse.error(Response.Status.CONFLICT)
                    .message("User exists with same username")
                    .addResult("user_id", userModel.getId())
                    .build();
        }

        if (request.getEmail() != null && !realm.isDuplicateEmailsAllowed()) {
            userModel = session.users().getUserByEmail(request.getEmail(), realm);
            if (userModel != null) {
                log.error("User exists with same email {}", request.getEmail());
                return JsonResponse.error(Response.Status.CONFLICT)
                        .message("User exists with same email")
                        .addResult("user_id", userModel.getId())
                        .build();
            }
        }
        return null;
    }

    private Response getUserResponse(UserRequest request, RealmModel realm, AdminAuth auth, boolean bss) {
        try {
            UserModel user = session.users().addUser(realm, request.getEmail());
            Set<String> emptySet = Collections.emptySet();

            updateUserFromRequest(user, request, emptySet, realm, session, false);
            if (!bss) {
                addUserPost(user, request);
            }

            new AdminEventBuilder(realm, auth, session, session.getContext().getConnection())
                    .realm(realm)
                    .resource(ResourceType.REALM)
                    .resource(ResourceType.USER)
                    .operation(OperationType.CREATE)
                    .resourcePath(session.getContext().getUri(), user.getId())
                    .success();

            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().commit();
            }
            return JsonResponse.success()
                    .httpStatus(Response.Status.CREATED)
                    .addResult("user_id", user.getId())
                    .build();

        } catch (ModelDuplicateException e) {
            return JsonResponse.error(Response.Status.CONFLICT)
                    .message("User exists with same username or email or phone")
                    .build();

        } catch (ModelException me) {
            log.warn("Could not create user", me);
            return JsonResponse.error(Response.Status.INTERNAL_SERVER_ERROR)
                    .message("Could not create user")
                    .build();
        } catch (NotFoundException e) {
            return JsonResponse.error(Response.Status.CONFLICT)
                    .message(e.getMessage())
                    .build();
        } finally {
            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().setRollbackOnly();
            }
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
    @Consumes("multipart/form-data")
    @NoCache
    public Response uploadUsers(MultipartFormDataInput file) throws IOException {
        List<InputPart> inputParts = file.getFormDataMap().get("file");
        if (inputParts == null || inputParts.isEmpty()) {
            return JsonResponse.error(Response.Status.BAD_REQUEST).build();
        }
        try {
            return JsonResponse.success()
                    .addResult("import-report",
                            userService.importUsers(inputParts.get(0).getBody(InputStream.class, null),
                                    getFileExtension(inputParts.get(0).getHeaders())))
                    .build();
        } catch (UnsupportedDataTypeException | FileServiceException e) {
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/downloadUsers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.MULTIPART_FORM_DATA)
    @NoCache
    public Response downloadUsers(DownloadUserRequest downloadUserRequest) throws IOException {
        try {
            byte[] bytes = userService.exportUsers(downloadUserRequest);
            if (bytes == null) {
                return JsonResponse
                        .fail()
                        .message("Users not found. Maybe database is empty")
                        .build();
            }
            Response.ResponseBuilder response = Response.ok((Object) bytes);
            response.header("Content-Disposition", "attachment; filename=\"users_info." + downloadUserRequest.getType() + "\"");
            return response.build();
        } catch (UnsupportedDataTypeException e) {
            return JsonResponse
                    .error(Response.Status.BAD_REQUEST)
                    .message(e.getMessage())
                    .build();
        }
    }

    private AdminAuth authenticateRealmAdminRequest(RealmModel realm) {
        String tokenString = new AppAuthManager().extractAuthorizationHeaderToken(session.getContext().getRequestHeaders());
        if (tokenString == null) throw new NotAuthorizedException("Bearer");
        AccessToken token;
        try {
            JWSInput input = new JWSInput(tokenString);
            token = input.readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            throw new NotAuthorizedException("Bearer token format error");
        }

        String realmName = token.getIssuer().substring(token.getIssuer().lastIndexOf('/') + 1);
        RealmManager realmManager = new RealmManager(session);
        RealmModel realmFromToken = realmManager.getRealmByName(realmName);
        if (realmFromToken == null) {
            throw new NotAuthorizedException("Unknown realm in token");
        }

        session.getContext().setRealm(realm);
        AuthenticationManager.AuthResult authResult = new AppAuthManager()
                .authenticateBearerToken(session, realm, session.getContext().getUri(), session.getContext().getConnection(), session.getContext().getRequestHeaders());
        if (authResult == null) {
            log.debug("Token not valid");
            throw new NotAuthorizedException("Bearer");
        }

        ClientModel client = realm.getClientByClientId(token.getIssuedFor());
        if (client == null) {
            throw new NotAuthorizedException("Could not find client for authorization");
        }

        AdminAuth auth = new AdminAuth(realm, authResult.getToken(), authResult.getUser(), client);

        AdminPermissions.evaluator(session, realm, auth).users().requireManage();

        if (!auth.getRealm().equals(realmManager.getKeycloakAdminstrationRealm())
                && !auth.getRealm().equals(realm)) {
            throw new ForbiddenException();
        }

        return auth;
    }


    private static void updateUserFromRequest(UserModel user, UserRequest request, Set<String> attrsToRemove, RealmModel realm, KeycloakSession session, boolean removeMissingRequiredActions) {
        if (request.getEmail() != null && realm.isEditUsernameAllowed()) {
            user.setUsername(request.getEmail());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
            if ("".equals(request.getEmail())) {
                user.setEmail(null);
            }
        }
        if (request.getName() != null) user.setFirstName(request.getName());

        user.setEmailVerified(true);
        user.setEnabled(true);

        List<String> reqActions = Collections.singletonList("UPDATE_PASSWORD");

        if (reqActions != null) {
            Set<String> allActions = new HashSet<>();
            for (ProviderFactory factory : session.getKeycloakSessionFactory().getProviderFactories(RequiredActionProvider.class)) {
                allActions.add(factory.getId());
            }
            for (String action : allActions) {
                if (reqActions.contains(action)) {
                    user.addRequiredAction(action);
                } else if (removeMissingRequiredActions) {
                    user.removeRequiredAction(action);
                }
            }
        }

        String phone = request.getPhone();
        if (phone != null)
            phone = phone.replaceAll("[^0-9]+", "");

        user.setAttribute(ATTR_PHONE_NAME, Collections.singletonList(phone));

    }

    private void addUserPost(UserModel userModel, UserRequest request) throws NotFoundException {
        UserPostRequest userPostRequest = DataMapper.toUserPostRequest(userModel, request);
        userPostRequest.setRoleId(DEFAULT_ROLE_ID);
        userPostService.save(userPostRequest);
    }

    private String getFileExtension(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {

                String[] name = filename.split("=");

                String finalFileName = name[1].trim().replaceAll("\"", "");

                return finalFileName.substring(finalFileName.lastIndexOf('.') + 1);
            }
        }
        return "unknown";
    }
}
