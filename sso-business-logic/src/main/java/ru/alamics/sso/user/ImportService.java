package ru.alamics.sso.user;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.Time;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.jpa.AdminEventEntity;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.*;
import ru.alamics.sso.jpa.entity.ImportUsersDataEntity;
import ru.alamics.sso.jpa.entity.ImportUsersReportEntity;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.jpa.repository.*;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.user.filetype.FileFactory;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.model.ImportResponse;
import ru.alamics.sso.user.model.ImportUsersDataModel;
import ru.alamics.sso.user.model.ImportUsersReportModel;
import ru.alamics.sso.user.model.UserRequest;
import ru.alamics.sso.util.validator.EmailValidator;
import ru.alamics.sso.util.validator.NotValidException;
import ru.alamics.sso.util.validator.PhoneValidator;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;

/**
    imports users from files

    same as UserExtService
*/
@Slf4j
@Stateless
@LocalBean
public class ImportService {

    @EJB
    private ImportUsersReportRepository importUsersReportRepository;
    @EJB
    private UserRepository userRepository;
    @EJB
    private RealmRepository realmRepository;
    @EJB
    private RoleRepository roleRepository;
    @EJB
    private AdminEventRepository adminEventRepository;
    @EJB
    private UserPostService userPostService;
    @EJB
    private MigrationService migrationService;
    @EJB
    private ImportReportService importReportService;

    public void createImportUsers(ImportUsersReportModel reportModel, List<ImportUsersDataModel> dataList) {

        if (FileFactory.CTL.equalsIgnoreCase(reportModel.getFiletype())) {
            migrationService.createImportUsers(reportModel, dataList);
        } else {
            importUsers(reportModel, dataList);
        }
    }

    private void importUsers(ImportUsersReportModel reportModel, List<ImportUsersDataModel> dataList) {
        log.info("Importing users from file {} in progress", reportModel.getName());
        int createdUsers = 0;
        int countClones = 0;
        int processedUsers = 0;

        if (dataList == null)
            dataList = importReportService.getDataList(reportModel.getId());

        try {
            for (ImportUsersDataModel data : dataList) {
                try {
                    data.setEmail(UserServiceUtil.doCleanMail(data.getEmail()));
                    data.setPhone(UserServiceUtil.doCleanPhone(data.getPhone()));

                    data.setErrors(null);
                    checkImportUser(reportModel.getRealmId(), data.getEmail(), data.getPhone());
                    UserEntity user = createUser(reportModel.getRealmId(), data);
                    createdUsers++;
                    data.setCreated(true);
                    data.setUserId(user.getId());
                    if (data.getTomsId() == null || data.getTomsId().isEmpty()) {
                        throw new NotFoundException("TomsId is not exist");
                    }
                    addUserPost(user, data);
                } catch (FoundException e) {
                    List<Object> errors = new LinkedList<>();
                    e.getResult().forEach((k, v) -> {
                        errors.add(v);
                    });
                    data.setErrors(errors.toString().substring(1, errors.toString().length() - 1));
                    countClones++;
                } catch (NotFoundException | NotValidException | FoundUserPostException e) {
                    data.setErrors(e.getMessage());
                    log.error("Importing user data is failed. {}", e.getMessage());
                } finally {
                    importReportService.updateImportUsersData(data);
                    processedUsers++;
                    if (processedUsers % 500 == 0) {
                        log.info("ProcessedUsers " + processedUsers);
                    }
                }
            }

            reportModel.setCountClones(countClones);
            reportModel.setCountCreatedUsers(createdUsers);
            reportModel.setStatus(ImportUsersReportStatus.DONE);

            importReportService.setReportDone(reportModel);

            createAdminEvent(OperationType.CREATE, reportModel, reportModel.getRealmId());
            log.info(String.format("Importing users from file %s is done: countUsers=%s, countCreatedUsers=%s, countClones=%s ",
                    reportModel.getName(), reportModel.getCountImportUsers(), reportModel.getCountCreatedUsers(),
                    reportModel.getCountClones()));

        } catch (Exception e) {
            log.error("Error, but processed " + processedUsers, e);
            throw e;
        }
    }

    private void checkImportUser(String realmId, String email, String phone) throws FoundException, NotValidException {

        EmailValidator.validate(email);
        PhoneValidator.validate(phone);

        FoundException foundException = new FoundException();
        try {
            checkOnExistUserByPhone(phone);
        } catch (FoundException e) {
            foundException.addResult("error1", e.getMessage());
        }
        try {
            checkOnExistUserByEmailAndUsername(realmId, email);
        } catch (FoundException e) {
            foundException.addResult("error2", e.getMessage());
        }

        if (foundException.getResult() != null) {
            throw foundException;
        }
    }

    private void checkOnExistUserByPhone(String phone) throws FoundException {
        UserEntity user = userRepository.getFirstUserByPhone(phone);

        if (user != null) {
            log.error("User exists with same phone {}", phone);
            throw new FoundException("User exists with same phone").addResult("userId", user.getId());
        }
    }

    private void checkOnExistUserByEmailAndUsername(String realmId, String email) throws FoundException {
        // Double-check duplicated username and email here due to federation
        UserEntity user = userRepository.getFirstUserByEmail(realmId, email);
        if (user != null) {
            log.error("User exists with same email {}", email);
            throw new FoundException("User exists with same email").addResult("userId", user.getId());
        }

        user = userRepository.getFirstUserByUsername(realmId, email);
        if (user != null) {
            log.error("User exists with same username {}", email);
            throw new FoundException("User exists with same username").addResult("userId", user.getId());
        }
    }

    private UserEntity createUser(String realmId, ImportUsersDataModel data) {

        UserEntity user = new UserEntity();
        user.setCreatedTimestamp(System.currentTimeMillis());
        user.setUsername(data.getEmail().toLowerCase());
        user.setEmail(data.getEmail().toLowerCase(), false);
        user.setFirstName(data.getFirstName());
        user.setRealmId(realmId);
        user.setEmailVerified(false);
        user.setEnabled(false);
        user = userRepository.save(user);

        RealmEntity realm = realmRepository.findRealmEntityById(realmId);
        if (realm.getDefaultRoles() != null && !realm.getDefaultRoles().isEmpty()) {
            UserEntity finalUser = user;
            realm.getDefaultRoles().forEach(o -> {
                UserRoleMappingEntity roleMapping = new UserRoleMappingEntity();
                roleMapping.setRoleId(o.getId());
                roleMapping.setUser(finalUser);
                roleRepository.save(roleMapping);
            });

            ClientEntity client = roleRepository.findClientByName("account", realmId);
            client.getDefaultRoles().forEach(o -> {
                UserRoleMappingEntity roleMapping = new UserRoleMappingEntity();
                roleMapping.setRoleId(o.getId());
                roleMapping.setUser(finalUser);
                roleRepository.save(roleMapping);
            });
        }

        UserAttributeEntity attributeEntity = new UserAttributeEntity();
        attributeEntity.setId(UUID.randomUUID().toString());
        attributeEntity.setName(ATTR_PHONE_NAME);
        attributeEntity.setUser(user);
        attributeEntity.setValue(data.getPhone());
        userRepository.saveAttributes(attributeEntity);

        return user;
    }

    private void createAdminEvent(OperationType operationType, ImportUsersReportModel report, String realmId) {
        AdminEventEntity adminEvent = new AdminEventEntity();
        adminEvent.setTime(Time.toMillis(Time.currentTime()));
        adminEvent.setRealmId(realmId);
        adminEvent.setOperationType(operationType.name());
        adminEvent.setAuthRealmId(realmId);
        adminEvent.setResourcePath("schedule/importUsersReport/" + report.getId());
        adminEvent.setResourceType("USER");
        adminEventRepository.save(adminEvent);
    }

    private void addUserPost(UserEntity user, ImportUsersDataModel data) throws NotFoundException, FoundUserPostException, NotValidException {
        UserPostRequest userPostRequest = new UserPostRequest();
        userPostRequest.setUserId(user.getId());
        userPostRequest.setTomsId(data.getTomsId());
        userPostRequest.setDmpId(data.getDmpId());

        userPostRequest.setRoleId(userPostService.getUserPostRole(data.getRole()));
        UserPostResponse userPostResponse = userPostService.save(userPostRequest);

        addSystemRoles(data, userPostResponse.getId());
    }

    private void addSystemRoles(ImportUsersDataModel userImport, String userPostId) throws javassist.NotFoundException {
        if (userImport.getSystems() == null || userImport.getSystems().isEmpty()) {
            return;
        }

        List<String> systems = Arrays.asList(userImport.getSystems().replaceAll("\\s", "").split(","));
        if (!systems.isEmpty()) {
            List<String> errorSystemNames = new LinkedList<>();

            for (String sysName : systems) {
                try {
                    userPostService.addSystemRole(UserMapper.toExternalSystemRoleRequest(userPostId,
                            userPostService.getExternalSystemRoleId(sysName)));
                } catch (NotFoundException e) {
                    errorSystemNames.add(sysName);
                }
            }

            if (!errorSystemNames.isEmpty()) {
                throw new NotFoundException(String.format("Not found roles for systems: systems=%s", errorSystemNames.toString()));
            }
        }
    }
}
