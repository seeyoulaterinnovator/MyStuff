package ru.alamics.sso.keycloak.create;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.*;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import ru.alamics.sso.keycloak.create.model.*;
import ru.alamics.sso.keycloak.mapper.DataMapper;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.keycloak.search.dto.UserDto;
import ru.alamics.sso.keycloak.search.rest.SearchResource;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.tbapi.TbapiService;
import ru.alamics.sso.remote.tbapi.TbapiServiceRestImpl;

import javax.persistence.EntityManager;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;

@Slf4j
public class UserService {

    protected KeycloakSession session;
    private AdminAuth auth;
    private RealmModel realm;
    private TbapiService tbapiService;

    public UserService(KeycloakSession session, AdminAuth auth) {
        this.auth = auth;
        this.session = session;
        realm = session.getContext().getRealm();
        tbapiService = new TbapiService(new TbapiServiceRestImpl());
    }

    private void commit() {
        if (session.getTransactionManager().isActive()) {
            session.getTransactionManager().commit();
        }
    }

    public byte[] exportUsers(DownloadUserRequest userRequest) throws IOException {
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

    public Response importUsers(InputStream inputStream, String type) throws IOException {
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
            if (!head.equalsIgnoreCase(UserParameter.EMAIL.getName()) && !head.equalsIgnoreCase(UserParameter.PHONE.getName()) &&
                    !head.equalsIgnoreCase(UserParameter.CUSTOMER.getName()) && !head.equalsIgnoreCase(UserParameter.ROLE.getName()) &&
                    !head.equalsIgnoreCase(UserParameter.SYSTEM.getName()) || headers.length != 5) {
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

    public String getFileExtension(MultivaluedMap<String, String> header) {

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

    public Response getUserResponse(UserRequest request) {
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

    private EntityManager getEM() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }
}
