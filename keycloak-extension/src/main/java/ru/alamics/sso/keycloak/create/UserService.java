package ru.alamics.sso.keycloak.create;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionProvider;
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
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.registration.tbapi.TbapiService;
import ru.alamics.sso.registration.tbapi.exception.TbapiRegisterException;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.remote.tbapi.TbapiServiceRestImpl;

import javax.activation.UnsupportedDataTypeException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.ValidationException;
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
    private UserFindService userFindService;

    public UserService(KeycloakSession session, AdminAuth auth, UserFindService userFindService) {
        this.auth = auth;
        this.session = session;
        this.userFindService = userFindService;
        realm = session.getContext().getRealm();
        tbapiService = new TbapiService(new TbapiServiceRestImpl());
        this.tbapiConnectConfig = TbapiConnectConfig.getStaticConfig();

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
        List<UserDto> userDto = new SearchResource(session).getUsers(realm.getName(), null, null, null, null, true);
        if (userDto == null || userDto.isEmpty()) {
            return null;
        }
        if (userRequest.getUserIds() != null && userRequest.getUserIds().length != 0) {
            userDto = searchUsersById(userDto, userRequest.getUserIds());
        }
        userDto = DataMapper.toGroupUserDtos(userDto);
        file.addRow(getUserParameterNames(userRequest.getUserParameters()));
        userDto.stream().forEach(o -> file.addRow(getUserParameters(o, userRequest.getUserParameters())));
        return file.save();
    }

    private List<UserDto> searchUsersById(List<UserDto> userDtos, String[] userIds) {
        List<UserDto> result = new LinkedList<>();
        userDtos.stream()
                .forEach(o -> {
                    for (String userId : userIds) {
                        if (o.getId().equals(userId)) {
                            result.add(o);
                        }
                    }
                });
        return result;
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
                case USER_ID:
                    parameters.add(userDto.getId());
                    break;
                case FIRST_NAME:
                    parameters.add(userDto.getFirstName());
                    break;
                case ENABLED:
                    parameters.add(userDto.getEnabled().toString());
                    break;
                case CUSTOMER:
                    parameters.add(userDto.getTomsId());
                    break;
                default:
                    parameters.add("");
            }
        }
        return parameters;
    }

    public ImportResponse importUsers(InputStream inputStream, String type, RealmModel realm) throws IOException, FileServiceException {
        log.info("Start upload users");
        this.realm = realm;

        FileModel file = FileFactory.createFileModel(inputStream, type);
        if (file == null) {
            throw new UnsupportedDataTypeException("Unsupported file format!");
        }
        checkStructure(file);

        List<String[]> rows = file.getRows();
        rows.remove(0);
        List<UserImport> userImports = DataMapper.toUserRequestList(rows);

        ImportResponse importResponse = new ImportResponse();
        //importResponse.setCountClones(getCountAndRemoveClones(userImports));
        createImportUsers(importResponse, userImports);
        log.info("Upload users success!", importResponse);
        return importResponse;
    }

    private void checkStructure(FileModel file) throws FileServiceException {
        String[] headers = file.getHeaders();
        for (String head : Arrays.asList(headers)) {
            if (!head.equalsIgnoreCase(UserParameter.FIRST_NAME.getName()) && !head.equalsIgnoreCase(UserParameter.EMAIL.getName()) &&
                    !head.equalsIgnoreCase(UserParameter.PHONE.getName()) && !head.equalsIgnoreCase(UserParameter.ORGANIZATION.getName()) &&
                    !head.equalsIgnoreCase(UserParameter.ROLE.getName()) && !head.equalsIgnoreCase(UserParameter.SYSTEM.getName()) ||
                    headers.length != 6 || file.getCountRows() < 2) {
                throw new FileServiceException("File Structure is not valid " +
                        "or 'csv' file encoding must be in UTF-8!");
            }
        }
    }

    private int getCountAndRemoveClones(List<UserImport> userImports) {
        log.info("findClonesFrom: {}", userImports);
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
        AtomicInteger createdUsers = new AtomicInteger();
        AtomicInteger tbapiErrors = new AtomicInteger();
        AtomicInteger tbapiSuccess = new AtomicInteger();
        AtomicInteger countClones = new AtomicInteger();
        userImports.stream().forEach(o -> {
            try {
                UserRequest userRequest = o.getUserRequest();
                checkImportUser(userRequest);
                UserModel user = createUser(userRequest);
                user.setEmailVerified(false);
                createAdminEvent(OperationType.CREATE, user);
                createdUsers.getAndIncrement();
                importResponse.addCreatedUserIds("userId", user.getId());

                Map<String, Object> tbapiResponse = tbapiService.registerUser(DataMapper.toUser(o),
                        tbapiConnectConfig);
                tbapiSuccess.getAndIncrement();

                if (tbapiResponse.get(UserConstants.ATTR_TOMS_NAME) == null) {
                    throw new TbapiRegisterException();
                }
                if (tbapiResponse.get(UserConstants.ATTR_DMP_NAME) != null) {
                    userRequest.setTomsId(tbapiResponse.get(UserConstants.ATTR_DMP_NAME).toString());
                }
                userRequest.setTomsId(tbapiResponse.get(UserConstants.ATTR_TOMS_NAME).toString());
                addUserPost(user, o);
            } catch (FoundException e) {
                e.getResult().forEach((k, v) -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", v);
                    error.put("importUserName", o.getUserRequest().getName());
                    importResponse.addError(error);
                });
                countClones.getAndIncrement();
            } catch (NotFoundException | ValidationException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", e.getMessage());
                error.put("importUserName", o.getUserRequest().getName());
                importResponse.addError(error);
            } catch (TbapiRegisterException e) {
                tbapiErrors.getAndIncrement();
            }
        });
        importResponse.setTbapiSuccess(tbapiSuccess);
        importResponse.setTbapiErrors(tbapiErrors);
        importResponse.setCreatedUsers(createdUsers);
        importResponse.setCountClones(countClones);
        commit();
    }

    private void checkImportUser(UserRequest userRequest) throws FoundException{
        validateUserPhoneAndEmail(userRequest);

        FoundException foundException = new FoundException();
        try {
            checkOnExistUserByPhone(userRequest, realm);
        } catch (FoundException e){
            foundException.addResult("error1", e.getMessage());
        }
        try {
            checkOnExistUserByEmailAndUsername(userRequest, realm);
        } catch (FoundException e){
            foundException.addResult("error2", e.getMessage());
        }

        if (foundException.getResult() != null){
            throw foundException;
        }
    }

    private UserModel createUser(UserRequest userRequest) {
        try {
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
        checkOnExistUser(request, realm);
        UserModel user = createUser(request);
        if (bss) {
            addUserPost(user, request);
        }
        createAdminEvent(OperationType.CREATE, user);
        commit();
        return user;
    }

    private void validateUserPhoneAndEmail(UserRequest userRequest) {
        String phone = userRequest.getPhone();
        String email = userRequest.getEmail();
        if (phone == null || !phone.matches("[\\d]+") || !phone.startsWith("7") || phone.length() != 11) {
            throw new ValidationException("Phone is not valid");
        }

        if (email == null || !email.contains("@") || !email.substring(0, 1).matches("([\\w[\\s]])+")
                || email.substring(0, 1).matches("[\\d]+") || email.contains(" ") ||
                !email.substring(email.indexOf("@") + 1, email.indexOf("@") + 2).matches("([\\w[\\s]])+")) {
            throw new ValidationException("Email is not valid");
        }
    }

    private void checkOnExistUser(UserRequest request, RealmModel realm) throws FoundException {
         checkOnExistUserByPhone(request, realm);
         checkOnExistUserByEmailAndUsername(request, realm);
    }

    private void checkOnExistUserByPhone(UserRequest request, RealmModel realm) throws FoundException {
        UserEntity user = userFindService.getUserByPhone(realm, request.getPhone());

        if (user != null) {
            log.error("User exists with same phone {}", request.getPhone());
            throw new FoundException("User exists with same phone").addResult("userId", user.getId());
        }
    }

    private void checkOnExistUserByEmailAndUsername(UserRequest request, RealmModel realm) throws FoundException {
        // Double-check duplicated username and email here due to federation
        if (request.getEmail() != null && !realm.isDuplicateEmailsAllowed()) {
            UserModel userModel = session.users().getUserByEmail(request.getEmail(), realm);
            if (userModel != null) {
                log.error("User exists with same email {}", request.getEmail());
                throw new FoundException("User exists with same email").addResult("userId", userModel.getId());
            }
        }

        UserModel userModel = session.users().getUserByUsername(request.getEmail(), realm);
        if (userModel != null) {
            log.error("User exists with same username {}", request.getEmail());
            throw new FoundException("User exists with same username").addResult("userId", userModel.getId());
        }
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
        if (userImport.getSystemNames() != null && !userImport.getSystemNames().isEmpty()) {
            for (String sysName : userImport.getSystemNames()) {
                userPostService.addSystemRole(DataMapper.toExternalSystemRoleRequest(userPostResponse.getId(),
                        userPostService.getExternalSystemRoleId(sysName)));
            }
        }
    }
}
