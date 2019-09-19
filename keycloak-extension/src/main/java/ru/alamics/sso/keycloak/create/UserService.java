package ru.alamics.sso.keycloak.create;

import javassist.NotFoundException;
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
import ru.alamics.sso.keycloak.search.dto.UserDto;
import ru.alamics.sso.keycloak.search.rest.SearchResource;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.model.UserConstants;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.registration.tbapi.TbapiService;
import ru.alamics.sso.registration.tbapi.exception.TbapiRegisterException;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.remote.tbapi.TbapiServiceRestImpl;

import javax.activation.UnsupportedDataTypeException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;

@Slf4j
public class UserService {
    private final static Long DEFAULT_ROLE_ID = 1L;   //Соответствует роли LPR
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
        if (file == null) {
            throw new UnsupportedDataTypeException("Unsupported file format!");
        }
        List<UserDto> userDto = new SearchResource(session).getUsers(null, null, null, null, true);
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
                    parameters.add(userDto.getRoleName());
                    break;
                case SYSTEM:
                    parameters.add(userDto.getSystemName());
                    break;
            }
        }
        return parameters;
    }

    public ImportResponse importUsers(InputStream inputStream, String type) throws IOException, FileServiceException {
        FileModel file = FileFactory.createFileModel(inputStream, type);
        if (file == null) {
            throw new UnsupportedDataTypeException("Unsupported file format!");
        }
        checkStructure(file);

        List<String[]> rows = file.getRows();
        rows.remove(0);
        List<UserImport> userImports = DataMapper.toUserRequestList(rows);

        ImportResponse importResponse = new ImportResponse();
        importResponse.setCountClones(getCountAndRemoveClones(userImports));
        createImportUsers(importResponse, userImports);
        return importResponse;
    }

    private void checkStructure(FileModel file) throws FileServiceException {
        String[] headers = file.getHeaders();
        for (String head : Arrays.asList(headers)) {
            if (!head.equalsIgnoreCase(UserParameter.EMAIL.getName()) && !head.equalsIgnoreCase(UserParameter.PHONE.getName()) &&
                    !head.equalsIgnoreCase(UserParameter.ORGANIZATION.getName()) && !head.equalsIgnoreCase(UserParameter.ROLE.getName()) &&
                    !head.equalsIgnoreCase(UserParameter.SYSTEM.getName()) || headers.length != 5 || file.getCountRows() < 2) {
                throw new FileServiceException("File Structure is not valid!");
            }
        }
    }

    private int getCountAndRemoveClones(List<UserImport> userImports) {
        int countClones = 0;
        List<UserImport> userRequestMain = new LinkedList<>();
        userImports.stream().forEach(o -> userRequestMain.add(o));
        for (int i = 0; i < userImports.size(); i++) {
            for (int j = i + 1; j < userImports.size(); j++) {
                if (userImports.get(i).getUserRequest().getEmail().equals(userImports.get(j).getUserRequest().getEmail()) ||
                        userImports.get(i).getUserRequest().getPhone().equals(userImports.get(j).getUserRequest().getPhone())) {
                    userImports.remove(j);
                    j--;
                    countClones++;
                }
            }
        }
        return countClones;
    }

    private void createImportUsers(ImportResponse importResponse, List<UserImport> userImports) {
        AtomicInteger tbapiErrors = new AtomicInteger();
        AtomicInteger tbapiSuccess = new AtomicInteger();
        userImports.stream().forEach(o -> {
            try {
                UserModel user = createUser(o.getUserRequest());
                createAdminEvent(OperationType.CREATE, user);
                importResponse.addCreatedUserIds("userId", user.getId());

                Map<String, Object> tbapiResponse = tbapiService.registerUser(DataMapper.toUser(o), tbapiConnectConfig);
                if (tbapiResponse.get(UserConstants.ATTR_TOMS_NAME) == null) {
                    throw new TbapiRegisterException();
                }
                if (tbapiResponse.get(UserConstants.ATTR_DMP_NAME) != null) {
                    o.getUserRequest().setTomsId(tbapiResponse.get(UserConstants.ATTR_DMP_NAME).toString());
                }
                o.getUserRequest().setTomsId(tbapiResponse.get(UserConstants.ATTR_TOMS_NAME).toString());
                addUserPost(user, o);

                tbapiSuccess.getAndIncrement();
            } catch (FoundException e) {
                Map<String, Object> error = new HashMap<>();
                error.put(e.getMessage(), e.getResult());
                error.put("userName", o.getUserRequest().getName());
                importResponse.addError(error);
            } catch (NotFoundException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", e.getMessage());
                error.put("userName", o.getUserRequest().getName());
                importResponse.addError(error);
            } catch (TbapiRegisterException e) {
                tbapiErrors.getAndIncrement();
            }
        });
        importResponse.setTbapiSuccess(tbapiSuccess);
        importResponse.setTbapiErrors(tbapiErrors);
        commit();
    }

    private UserModel createUser(UserRequest userRequest) throws FoundException {
        try {
            checkOnExistUser(userRequest, realm);
            UserModel user = session.users().addUser(realm, userRequest.getEmail());
            updateUserFromRequest(user, userRequest, realm, session, false);
            return user;
        } finally {
            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().setRollbackOnly();
            }
        }
    }

    private static void updateUserFromRequest(UserModel user, UserRequest
            request, RealmModel realm, KeycloakSession session, boolean removeMissingRequiredActions) {
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

        user.setAttribute(ATTR_PHONE_NAME, Collections.singletonList(request.getPhone()));
    }

    public UserModel createUser(UserRequest request, boolean bss) throws FoundException, NotFoundException {
        UserModel user = createUser(request);
        if (bss) {
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
            throw new FoundException("User exists with same phone").addResult("userId", users.get(0).getId());
        }

        // Double-check duplicated username and email here due to federation
        UserModel userModel = session.users().getUserByUsername(request.getEmail(), realm);
        if (userModel != null) {
            log.error("User exists with same username {}", request.getEmail());
            throw new FoundException("User exists with same username").addResult("userId", userModel.getId());
        }

        if (request.getEmail() != null && !realm.isDuplicateEmailsAllowed()) {
            userModel = session.users().getUserByEmail(request.getEmail(), realm);
            if (userModel != null) {
                log.error("User exists with same email {}", request.getEmail());
                throw new FoundException("User exists with same email").addResult("userId", userModel.getId());
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

    private void addUserPost(UserModel userModel, UserRequest request) throws NotFoundException {
        UserPostRequest userPostRequest = DataMapper.toUserPostRequest(userModel, request);
        userPostRequest.setRoleId(DEFAULT_ROLE_ID);
        userPostService.save(userPostRequest);
    }

    private void addUserPost(UserModel userModel, UserImport userImport) throws NotFoundException {
        UserPostRequest userPostRequest = DataMapper.toUserPostRequest(userModel, userImport.getUserRequest());
        userPostRequest.setRoleId(userPostService.getUserPostRole(userImport.getRoleName()));
        UserPostResponse userPostResponse = userPostService.save(userPostRequest);
        userPostService.addSystemRole(DataMapper.toExternalSystemRoleRequest(userPostResponse.getId(),
                userPostService.getExternalSystemRoleId(userImport.getSystemName())));
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
