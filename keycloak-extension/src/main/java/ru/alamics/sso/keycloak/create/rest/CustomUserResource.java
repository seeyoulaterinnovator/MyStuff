package ru.alamics.sso.keycloak.create.rest;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.keycloak.models.*;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.admin.AdminAuth;
import ru.alamics.sso.user.FileServiceException;
import ru.alamics.sso.user.ImportUserHistoryService;
import ru.alamics.sso.user.UserService;
import ru.alamics.sso.user.UserServiceImpl;
import ru.alamics.sso.user.model.*;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.service.UserFindService;

import javax.activation.UnsupportedDataTypeException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;

@Slf4j
public class CustomUserResource {
    protected KeycloakSession session;
    private UserService userService;
    private ImportUserHistoryService importUserHistoryService;

    public CustomUserResource(KeycloakSession session, AdminAuth auth) {
        this.session = session;
//        AdminAuth auth = authenticateRealmAdminRequest(session.getContext().getRealm());
        this.userService = new UserServiceImpl(session, auth);
        try {
            this.importUserHistoryService = (ImportUserHistoryService) new InitialContext().lookup("java:global/domru-sso/" + ImportUserHistoryService.class.getSimpleName());
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
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
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @NoCache
    public Response uploadUsers(@MultipartForm FileDto file, @HeaderParam(HttpHeaders.CONTENT_DISPOSITION) String content) {

        if (file == null ||
                content == null || content.isBlank()) {
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
    @Path("/uploadImportUsersFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @NoCache
    public Response deferredUploadUsers(@MultipartForm FileDto file, @HeaderParam(HttpHeaders.CONTENT_DISPOSITION) String content) {
        if (file == null || content == null || content.isBlank()) {
            return JsonResponse.error(Response.Status.BAD_REQUEST).build();
        }
        try (InputStream bas = new ByteArrayInputStream(file.getFileData())) {
            userService.deferredImportUsers(bas, content);
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
    @Path("/importUserHistory")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImportUserHistoriesByRealm() {
        return JsonResponse.success()
                .addResult("importUserHistories", importUserHistoryService.getImportUserHistories(session.getContext().getRealm().getName()))
                .build();
    }

    @POST
    @Path("/downloadImportUsersReport/{id}")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public Response downloadImportUsersReport(@PathParam("id") String importId) {
        try {
            log.info("Start download users");
            FileModel file = userService.downloadUsersByImportReportId(importId);
            Response.ResponseBuilder response = Response.ok((Object) file.save());
            if (file instanceof XlsxImpl) {
                response.header("Content-Disposition", "attachment; filename=\"users_info.xlsx" + "\"");
                response.header("filename", "import_users_report.xlsx");
                response.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            } else {
                response.header("Content-Disposition", "attachment; filename=\"users_info.csv" + "\"");
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
}
