package ru.alamics.sso.keycloak.create;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
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
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.model.UserConstants;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.registration.tbapi.TbapiService;
import ru.alamics.sso.registration.tbapi.exception.TbapiRegisterException;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.remote.tbapi.TbapiServiceRestImpl;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;

@Slf4j
public class UserService {

    protected KeycloakSession session;
    private AdminAuth auth;
    private RealmModel realm;
    private TbapiService tbapiService;
    private UserPostService userPostService;
    private TbapiConnectConfig tbapiConnectConfig;

    public UserService(KeycloakSession session, AdminAuth auth) {
        this.auth = auth;
        this.session = session;
        realm = session.getContext().getRealm();
        tbapiService = new TbapiService(new TbapiServiceRestImpl());
        createTbapiConnectConfig();

        try {
            this.userPostService = (UserPostService) new InitialContext().lookup("java:global/domru-sso/" + UserPostService.class.getSimpleName());
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
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
                    parameters.add("");
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
        AtomicInteger tbapiErrors = new AtomicInteger();
        AtomicInteger tbapiSuccess = new AtomicInteger();
        userRequests.stream().forEach(o -> {
            try {
                UserModel user = createUser(o);
                createAdminEvent(OperationType.CREATE, user);
                successResponse.add("userId : " + user.getId());

                Map<String, Object> tbapiResponse = tbapiService.registerUser(DataMapper.toUser(o), tbapiConnectConfig);
                if (tbapiResponse.get(UserConstants.ATTR_TOMS_NAME) == null){
                   throw new TbapiRegisterException();
                }
                if (tbapiResponse.get(UserConstants.ATTR_DMP_NAME) != null){
                    o.setTomsId(tbapiResponse.get(UserConstants.ATTR_DMP_NAME).toString());
                }
                o.setTomsId(tbapiResponse.get(UserConstants.ATTR_TOMS_NAME).toString());
                addUserPost(user, o);

                tbapiSuccess.getAndIncrement();
            } catch (FoundException e) {
                errorResponse.add("userName : " + o.getName());
            } catch (TbapiRegisterException e) {
                tbapiErrors.getAndIncrement();
            }
        });
        if (session.getTransactionManager().isActive()) {
            session.getTransactionManager().commit();
        }
        JsonResponse jsonResponse = new JsonResponse();
        jsonResponse.addResult("success", successResponse);
        jsonResponse.addResult("error", errorResponse);
        jsonResponse.addResult("tbapiSuccess", tbapiSuccess);
        jsonResponse.addResult("tbapiErrors", tbapiErrors);
        return jsonResponse;
    }

    private UserModel createUser(UserRequest userRequest) throws FoundException {
        checkOnExistUser(userRequest, realm);
        UserModel user = session.users().addUser(realm, userRequest.getEmail());
        Set<String> emptySet = Collections.emptySet();
        updateUserFromRequest(user, userRequest, emptySet, realm, session, false);
        return user;
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

    public UserModel createUser(UserRequest request, boolean bss) throws FoundException {
        UserModel user = createUser(request);
        if (!bss) {
            addUserPost(user, request);
        }
        createAdminEvent(OperationType.CREATE, user);
        commit();
        return user;
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

    private void createAdminEvent(OperationType operationType, UserModel user) {
        new AdminEventBuilder(realm, auth, session, session.getContext().getConnection())
                .realm(realm)
                .resource(ResourceType.REALM)
                .resource(ResourceType.USER)
                .operation(operationType)
                .resourcePath(session.getContext().getUri(), user.getId())
                .success();
    }

    private void addUserPost(UserModel userModel, UserRequest request) throws FoundException {
        UserPostRequest userPostRequest = DataMapper.toUserPostRequest(userModel, request);
        userPostRequest.setRoleId(1L);
        userPostService.save(userPostRequest);
    }

    private void createTbapiConnectConfig() {
        TbapiConnectConfig connectConfig = new TbapiConnectConfig();

        connectConfig.setHost("tb-app01.int.bss.loc");
        connectConfig.setPort(26300);
        connectConfig.setAppname("SSP");
        connectConfig.setUsername("anonymous");
        connectConfig.setPath("/api/v1/customerManagement/customerAccount");
        connectConfig.setSecure(false);
        this.tbapiConnectConfig = connectConfig;
    }
}
