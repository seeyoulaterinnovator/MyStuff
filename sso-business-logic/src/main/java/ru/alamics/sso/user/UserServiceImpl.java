package ru.alamics.sso.user;

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
import ru.alamics.sso.keycloak.entity.ImportUserDataEntity;
import ru.alamics.sso.keycloak.entity.ImportUserHistoryEntity;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.model.*;
import ru.alamics.sso.user.web.ImportUserHistoryDto;
import ru.alamics.sso.user.web.UserSearchDto;
import ru.alamics.sso.util.Util;

import javax.activation.UnsupportedDataTypeException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.ValidationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;
import static ru.alamics.sso.user.model.UserParameter.*;

@Slf4j
public class UserServiceImpl implements UserService {
    private final static Long DEFAULT_ROLE_ID = 1L;   //Соответствует роли LPR
    protected KeycloakSession session;
    private AdminAuth auth;
    private RealmModel realm;
    private UserPostService userPostService;
    private UserFindService userFindService;
    private ImportUserHistoryService importUserHistoryService;

    public UserServiceImpl(KeycloakSession session, AdminAuth auth) {
        this.auth = auth;
        this.session = session;
        realm = session.getContext().getRealm();
        try {
            this.userFindService = (UserFindService) new InitialContext().lookup("java:global/domru-sso/" + UserFindService.class.getSimpleName());
            this.userPostService = (UserPostService) new InitialContext().lookup("java:global/domru-sso/" + UserPostService.class.getSimpleName());
            this.importUserHistoryService = (ImportUserHistoryService) new InitialContext().lookup("java:global/domru-sso/" + ImportUserHistoryService.class.getSimpleName());
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

    @Override
    public byte[] exportUsers(DownloadUserRequest userRequest) throws IOException {
        FileModel file = FileFactory.createFileModel(userRequest.getType());
        if (file == null) {
            throw new UnsupportedDataTypeException("Unsupported file format!");
        }
        List<UserSearchDto> userDto = userFindService.getUsersByParameters(realm.getName(), null, null, null, null, true);
        if (userDto == null || userDto.isEmpty()) {
            return null;
        }
        if (userRequest.getUserIds() != null && userRequest.getUserIds().length != 0) {
            userDto = searchUsersById(userDto, userRequest.getUserIds());
        }
        userDto = UserMapper.toGroupUserDtos(userDto);
        file.addRow(getUserParameterNames(userRequest.getUserParameters()));
        userDto.stream().forEach(o -> file.addRow(getUserParameters(o, userRequest.getUserParameters())));
        return file.save();
    }

    @Override
    public FileModel downloadUsersByImportReportId(String importId) throws IOException {
        ImportUserHistoryEntity importUserHistory = importUserHistoryService.getImportUserHistory(importId);
        FileModel file = FileFactory.createFileModel(importUserHistory.getName().substring(importUserHistory.getName().lastIndexOf(".")+1));
        if (file == null) {
            throw new UnsupportedDataTypeException("Unsupported file format!");
        }
        List<String> userParameterNames = getUserParameterNames(UserParameter.values());
        List<String> finishParameterNames = userParameterNames.stream().skip(1).limit(userParameterNames.size()-2).collect(Collectors.toList());
        finishParameterNames.addAll(List.of("Статус пользователя", "Ошибки"));
        file.addRow(finishParameterNames);
        importUserHistory.getImportUserData().stream()
                .forEach(o -> {
                    List<String> list = new LinkedList<>();
                    list.add(o.getFirstName());
                    list.add(o.getEmail());
                    list.add(o.getPhone());
                    list.add(o.getTomsId());
                    list.add(o.getDmpId());
                    list.add(o.getRole());
                    list.add(o.getSystems());
                    list.add(String.valueOf(o.isCreated()));
                    list.add(o.getErrors());
                    file.addRow(list);
                });
        return file;
    }

    private List<UserSearchDto> searchUsersById(List<UserSearchDto> userDtos, String[] userIds) {
        List<UserSearchDto> result = new LinkedList<>();
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

    private List<String> getUserParameters(UserSearchDto userDto, UserParameter[] userParameters) {
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
                case TOMS_ID:
                    parameters.add(userDto.getTomsId());
                    break;
                case DMP_ID:
                    parameters.add(userDto.getDmpId());
                    break;
                default:
                    parameters.add("");
            }
        }
        return parameters;
    }

    @Override
    public ImportResponse importUsers(InputStream inputStream, String content) throws IOException, FileServiceException {
        log.info("Start upload users");

        FileModel file = FileFactory.createFileModel(inputStream, getFileExtension(content));
        if (file == null) {
            throw new UnsupportedDataTypeException("Unsupported file format!");
        }
        checkStructure(file);

        List<String[]> rows = file.getRows();
        rows.remove(0);
        List<ImportUserDataEntity> userImports = UserMapper.toUserRequestList(rows);
        ImportResponse importResponse = createImportUsers(userImports);
        importUserHistoryService.saveImportUserHistory(UserMapper.toImportUserHistoryEntity(realm.getName(), getFileName(content), userImports, importResponse));

        log.info("Upload users success!", importResponse);
        return importResponse;
    }

    @Override
    public void uploadImportUsersFile(InputStream inputStream, String content) throws IOException, FileServiceException {
        log.info("Start deferred upload users");

        FileModel file = FileFactory.createFileModel(inputStream, getFileExtension(content));
        if (file == null) {
            throw new UnsupportedDataTypeException("Unsupported file format!");
        }
        checkStructure(file);

        List<String[]> rows = file.getRows();
        rows.remove(0);
        ImportUserHistoryEntity importUserHistory = UserMapper.toImportUserHistoryEntity(realm.getName(), getFileName(content), UserMapper.toUserRequestList(rows));
        importUserHistory.setDone(false);
        importUserHistoryService.saveImportUserHistory(importUserHistory);

        log.info("Upload deferred users success!");
    }

    private String getFileExtension(String content) {
        String finalFileName = getFileName(content);
        return finalFileName.substring(finalFileName.lastIndexOf('.') + 1);
    }

    private String getFileName(String content) {
        String[] contentDisposition = content.split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                return name[1].trim().replaceAll("\"", "");
            }
        }
        return "unknown";
    }

    private void checkStructure(FileModel file) throws FileServiceException {
        String[] headers = file.getHeaders();
        if (headers.length != 7 || file.getCountRows() < 2) {
            throw new FileServiceException("File Structure is not valid! Count columns not valid or data is empty!");
        }
        checkHeaders(headers);
    }

    private void checkHeaders(String[] headers) throws FileServiceException {
        for (int i = 0; i < headers.length; i++) {
            switch (i) {
                case 0:
                    checkHeader(headers[i], FIRST_NAME);
                    break;
                case 1:
                    checkHeader(headers[i], EMAIL);
                    break;
                case 2:
                    checkHeader(headers[i], PHONE);
                    break;
                case 3:
                    checkHeader(headers[i], TOMS_ID);
                    break;
                case 4:
                    checkHeader(headers[i], DMP_ID);
                    break;
                case 5:
                    checkHeader(headers[i], ROLE);
                    break;
                case 6:
                    checkHeader(headers[i], SYSTEM);
                    break;
            }
        }
    }

    private void checkHeader(String head, UserParameter userParameter) throws FileServiceException {
        if (!head.equalsIgnoreCase(userParameter.getName())) {
            throw new FileServiceException("File Structure is not valid! Header is not valid");
        }
    }

    private ImportResponse createImportUsers(List<ImportUserDataEntity> userImports) {
        ImportResponse importResponse = new ImportResponse();

        AtomicInteger createdUsers = new AtomicInteger();
        AtomicInteger countClones = new AtomicInteger();
        userImports.stream().forEach(o -> {
            try {
                UserRequest userRequest = UserMapper.toUserRequest(o);
                checkImportUser(userRequest);
                UserModel user = createUser(userRequest);
                user.setEmailVerified(false);
                createAdminEvent(OperationType.CREATE, user);
                createdUsers.getAndIncrement();
                o.setCreated(true);
                importResponse.addCreatedUserIds("userId", user.getId());

                if (userRequest.getTomsId() == null || userRequest.getTomsId().isBlank()) {
                    throw new NotFoundException("TomsId is not exist");
                }
                addUserPost(user, o, userRequest);
            } catch (FoundException e) {
                e.getResult().forEach((k, v) -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", v);
                    error.put("importUserName", o.getFirstName());
                    importResponse.addError(error);
                });
                countClones.getAndIncrement();
            } catch (NotFoundException | ValidationException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", e.getMessage());
                error.put("importUserName", o.getFirstName());
                importResponse.addError(error);
            }
        });
        importResponse.setCreatedUsers(createdUsers);
        importResponse.setCountClones(countClones);
        commit();

        return importResponse;
    }

    private void checkImportUser(UserRequest userRequest) throws FoundException {
        Util.validateUserPhoneAndEmail(userRequest.getEmail(), userRequest.getPhone());

        FoundException foundException = new FoundException();
        try {
            checkOnExistUserByPhone(userRequest, realm);
        } catch (FoundException e) {
            foundException.addResult("error1", e.getMessage());
        }
        try {
            checkOnExistUserByEmailAndUsername(userRequest, realm);
        } catch (FoundException e) {
            foundException.addResult("error2", e.getMessage());
        }

        if (foundException.getResult() != null) {
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

    @Override
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
        UserPostRequest userPostRequest = UserMapper.toUserPostRequest(userModel, request);
        userPostRequest.setRoleId(DEFAULT_ROLE_ID);
        userPostService.save(userPostRequest);
    }

    private void addUserPost(UserModel userModel, ImportUserDataEntity userImport, UserRequest userRequest) throws NotFoundException {
        UserPostRequest userPostRequest = UserMapper.toUserPostRequest(userModel, userRequest);
        userPostRequest.setRoleId(userPostService.getUserPostRole(userImport.getRole()));
        UserPostResponse userPostResponse = userPostService.save(userPostRequest);

        addSystemRoles(userImport, userPostResponse.getId());
    }

    private void addSystemRoles(ImportUserDataEntity userImport, String userPostId) throws NotFoundException {
        List<String> systems = List.of(userImport.getSystems().replaceAll("\\s", "").split(","));
        if (systems != null && !systems.isEmpty()) {
            for (String sysName : systems) {
                userPostService.addSystemRole(UserMapper.toExternalSystemRoleRequest(userPostId,
                        userPostService.getExternalSystemRoleId(sysName)));
            }
        }
    }
}
