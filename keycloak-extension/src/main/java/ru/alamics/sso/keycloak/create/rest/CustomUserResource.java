package ru.alamics.sso.keycloak.create.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.util.IOUtils;
import org.bouncycastle.asn1.ocsp.ResponseBytes;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.specimpl.BuiltResponse;
import org.jboss.resteasy.specimpl.ResponseBuilderImpl;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.*;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ForbiddenException;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;
import ru.alamics.sso.keycloak.create.FileServiceException;
import ru.alamics.sso.keycloak.create.model.*;
import ru.alamics.sso.keycloak.mapper.DataMapper;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.keycloak.response.ResponseBuilder;
import ru.alamics.sso.keycloak.search.dto.UserDto;
import ru.alamics.sso.keycloak.search.rest.SearchResource;
import ru.alamics.sso.registration.FoundException;

import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;

@Slf4j
public class CustomUserResource {

    private final static String EMAIL = "E-mail";
    private final static String PHONE = "Телефон";
    private final static String CUSTOMER = "ID customer";
    private final static String ROLE = "Роли пользователя";
    private final static String SYSTEM = "Целевая система";

    protected KeycloakSession session;
    private AdminAuth auth;
    private RealmModel realm;

    public CustomUserResource(KeycloakSession session) {
        this.session = session;
        auth = authenticateRealmAdminRequest(session.getContext().getRealm());
        realm = session.getContext().getRealm();
    }

    @POST
    @Path("")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(final UserRequest request, final HttpHeaders headers) {
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            return ErrorResponse.error("Phone is required attribute", Response.Status.BAD_REQUEST);
        }

        return getUserResponse(request);
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
        return importUsers(inputParts.get(0).getBody(InputStream.class, null), getFileExtension(inputParts.get(0).getHeaders()));
    }

    @GET
    @Path("/downloadUsers")
    @Produces(MediaType.MULTIPART_FORM_DATA)
    @NoCache
    public Response downloadUsers(DownloadUserRequest downloadUserRequest) throws IOException {
        byte[] bytes = ((ByteArrayOutputStream) exportUsers(downloadUserRequest)).toByteArray();
        Response.ResponseBuilder response = Response.ok((Object) bytes);
        response.header("Content-Disposition", "attachment; filename=\"users_info.xlsx\"");
        return response.build();
    }

    private void checkOnExistUser(UserRequest request, RealmModel realm) throws FoundException {
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
            throw new FoundException("User exists with same phone");
        }

        // Double-check duplicated username and email here due to federation
        UserModel userModel = session.users().getUserByUsername(request.getEmail(), realm);
        if (userModel != null) {
            log.error("User exists with same username {}", request.getEmail());
            throw new FoundException("User exists with same username");
        }

        if (request.getEmail() != null && !realm.isDuplicateEmailsAllowed()) {
            userModel = session.users().getUserByEmail(request.getEmail(), realm);
            if (userModel != null) {
                log.error("User exists with same email {}", request.getEmail());
                throw new FoundException("User exists with same email");
            }
        }
    }


    private void commit() {
        if (session.getTransactionManager().isActive()) {
            session.getTransactionManager().commit();
        }
    }

    private Response getUserResponse(UserRequest request) {
        try {
            UserModel user = createUser(request);

            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().commit();
            }
            return JsonResponse.success()
                    .httpStatus(Response.Status.CREATED)
                    .addResult("user_id", user.getId())
                    .build();

        } catch (ModelDuplicateException e) {
            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().setRollbackOnly();
            }
            return JsonResponse.error(Response.Status.CONFLICT)
                    .message("User exists with same username or email or phone")
                    .build();

        } catch (ModelException me) {
            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().setRollbackOnly();
            }
            log.warn("Could not create user", me);
            return JsonResponse.error(Response.Status.INTERNAL_SERVER_ERROR)
                    .message("Could not create user")
                    .build();
        } catch (FoundException e) {
            return JsonResponse
                    .error(Response.Status.CONFLICT)
                    .message(e.getMessage())
                    .build();
        }
    }

    private EntityManager getEM() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
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
            if ("" .equals(request.getEmail())) {
                user.setEmail(null);
            }
        }
        if (request.getName() != null) user.setFirstName(request.getName());

        user.setEmailVerified(true);

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

        user.setAttribute(ATTR_PHONE_NAME, Collections.singletonList(request.getPhone()));
    }

    private OutputStream exportUsers(DownloadUserRequest userRequest) throws IOException {
        FileModel file = FileFactory.createFileModel(userRequest.getType());

        List<UserDto> userDto = new SearchResource(session).getUsers("", "", "");
        if (userDto == null || userDto.isEmpty()) {
            return null;
        }
        file.addRow(getUserParameterNames(userRequest.getUserParameters()));
        userDto.stream().forEach(o -> file.addRow(getUserParameters(o, userRequest.getUserParameters())));
        return file.save();
    }

    private List<String> getUserParameterNames(UserParameter[] userParameters) {
        List<String> names = new LinkedList<>();
        for (UserParameter userParameter : userParameters) {
            names.add(userParameter.getName());
        }
        return names;
    }

    private List<String> getUserParameters(UserDto userDto, UserParameter[] userParameters) {
        List<String> parameters = new LinkedList<>();
        for (UserParameter userParameter : userParameters) {
            switch (userParameter) {
                case EMAIL:
                    parameters.add(userDto.getEmail());
                    break;
                case PHONE:
                    parameters.add(userDto.getPhone());
                    break;
                case ROLE:
                    parameters.add(userDto.getRoleId());
                    break;
                case CUSTOMER:
                    parameters.add(userDto.getTomsId());
                    break;
                case SYSTEM:
                    parameters.add(userDto.getAccessId());
                    break;
            }
        }
        return parameters;
    }

    private Response importUsers(InputStream inputStream, String type) throws IOException {
        FileModel file = FileFactory.createFileModel(inputStream, type);
        if (file == null) {
            return JsonResponse.fail().message("Unsupported file format!").build();
        }

        String[] headers = file.getHeaders();
        try {
            checkStructure(headers);
        } catch (FileServiceException e) {
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
        List<String[]> rows = file.getRows();
        rows.remove(0);
        List<UserRequest> userRequests = DataMapper.toUserRequestList(rows);
        if (userRequests == null || userRequests.isEmpty()) {
            JsonResponse.fail()
                    .message("File Structure is empty!")
                    .build();
        }
        int countClones = getCountClones(userRequests);
        return JsonResponse.success()
                .addResult("count clones", countClones)
                .addResult("create users", createUsers(userRequests))
                .build();
    }

    private void checkStructure(String[] headers) throws FileServiceException {

        for (String head : Arrays.asList(headers)) {
            if (!head.equalsIgnoreCase(EMAIL) && !head.equalsIgnoreCase(PHONE) && !head.equalsIgnoreCase(CUSTOMER)
                    && !head.equalsIgnoreCase(ROLE) && !head.equalsIgnoreCase(SYSTEM) || headers.length != 5) {
                throw new FileServiceException("File Structure is not valid!");
            }
        }
    }

    private int getCountClones(List<UserRequest> userRequests) {
        int countClones = 0;
        List<UserRequest> userRequestMain = new LinkedList<>();
        userRequests.stream().forEach(o -> userRequestMain.add(o));
        for (int i = 0; i < userRequests.size(); i++) {
            for (int j = i + 1; j < userRequests.size(); j++) {
                if (userRequests.get(i).getEmail().equals(userRequests.get(j).getEmail()) ||
                        userRequests.get(i).getPhone().equals(userRequests.get(j).getPhone())) {
                    userRequests.remove(j);
                    j--;
                    countClones++;
                }
            }
        }
        return countClones;
    }

    private JsonResponse createUsers(List<UserRequest> userRequests) {
        List<String> successResponse = new LinkedList<>();
        List<String> errorResponse = new LinkedList<>();
        userRequests.stream().forEach(o -> {
            try {
                successResponse.add("userId : " + createUser(o).getId());
            } catch (FoundException e) {
                errorResponse.add("userName : " + o.getName());
            }
        });
        if (session.getTransactionManager().isActive()) {
            session.getTransactionManager().commit();
        }
        JsonResponse jsonResponse = new JsonResponse();
        jsonResponse.addResult("success", successResponse);
        jsonResponse.addResult("error", errorResponse);
        return jsonResponse;
    }

    private UserModel createUser(UserRequest userRequest) throws FoundException {
        checkOnExistUser(userRequest, realm);
        UserModel user = session.users().addUser(realm, userRequest.getEmail());
        Set<String> emptySet = Collections.emptySet();
        updateUserFromRequest(user, userRequest, emptySet, realm, session, false);
        new AdminEventBuilder(realm, auth, session, session.getContext().getConnection())
                .resource(ResourceType.USER)
                .operation(OperationType.CREATE)
                .resourcePath(session.getContext().getUri(), user.getId())
                .representation(userRequest)
                .success();
        return user;
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
