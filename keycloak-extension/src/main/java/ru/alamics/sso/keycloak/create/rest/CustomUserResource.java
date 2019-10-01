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
import ru.alamics.sso.registration.service.UserFindService;

import javax.activation.UnsupportedDataTypeException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
public class CustomUserResource {
    protected KeycloakSession session;
    private UserService userService;

    public CustomUserResource(KeycloakSession session, UserFindService userFindService) {
        this.session = session;
        AdminAuth auth = authenticateRealmAdminRequest(session.getContext().getRealm());
        this.userService = new UserService(session, auth, userFindService);
    }

    @POST
    @Path("")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(final UserRequest request, final HttpHeaders headers) {
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            return ErrorResponse.error("Phone is required attribute", Response.Status.BAD_REQUEST);
        }
        return getUserResponse(request, false);
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

        return getUserResponse(request, true);
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
            return JsonResponse.error(Response.Status.CONFLICT)
                    .message("User exists with same username or email or phone")
                    .build();
        } catch (ModelException me) {
            log.error("Could not create user", me);
            return JsonResponse.error(Response.Status.INTERNAL_SERVER_ERROR)
                    .message("Could not create user")
                    .build();
        } catch (NotFoundException e) {
            log.error("Could not create user", e);
            return JsonResponse.error(Response.Status.FOUND)
                    .message(e.getMessage())
                    .build();
        } catch (FoundException e) {
            log.error("Could not create user", e);
            return JsonResponse.error(Response.Status.CONFLICT)
                    .message(e.getMessage())
                    .addResult("info", e.getResult())
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
    @Consumes("multipart/form-data")
    @NoCache
    public Response uploadUsers(MultipartFormDataInput file) {
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
                log.warn("Users not found. Maybe database is empty");
                return JsonResponse
                        .fail()
                        .message("Users not found. Maybe database is empty")
                        .build();
            }
            Response.ResponseBuilder response = Response.ok((Object) bytes);
            response.header("Content-Disposition", "attachment; filename=\"users_info." + downloadUserRequest.getType() + "\"");
            if(downloadUserRequest.getType().equals("xlsx")) {
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
