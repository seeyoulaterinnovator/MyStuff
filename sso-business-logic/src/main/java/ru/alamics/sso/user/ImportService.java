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

    public void createImportUsers(ImportUsersReportEntity importUsersReport) {

        if (FileFactory.CTL.equalsIgnoreCase(importUsersReport.getFiletype())) {
            migrationService.createImportUsers(importUsersReport);
        } else {
            importUsers(importUsersReport);
        }
    }

    private void importUsers(ImportUsersReportEntity importUsersReport) {
        log.info("importing users from file {} in progress", importUsersReport.getName());
        AtomicInteger createdUsers = new AtomicInteger();
        AtomicInteger countClones = new AtomicInteger();
        int processedUsers = 0;

        try {
            for (ImportUsersDataEntity o : importUsersReport.getImportUserData()) {
                try {
                    o.setEmail(UserServiceUtil.doCleanMail(o.getEmail()));
                    o.setPhone(UserServiceUtil.doCleanPhone(o.getPhone()));

                    o.setErrors(null);
                    checkImportUser(importUsersReport.getRealmId(), o.getEmail(), o.getPhone());
                    UserEntity user = createUser(importUsersReport.getRealmId(), o);
                    createdUsers.getAndIncrement();
                    o.setCreated(true);
                    o.setUserId(user.getId());
                    if (o.getTomsId() == null || o.getTomsId().isEmpty()) {
                        throw new NotFoundException("TomsId is not exist");
                    }
                    addUserPost(user, o);
                } catch (FoundException e) {
                    List<Object> errors = new LinkedList<>();
                    e.getResult().forEach((k, v) -> {
                        errors.add(v);
                    });
                    o.setErrors(errors.toString().substring(1, errors.toString().length() - 1));
                    countClones.getAndIncrement();
                } catch (NotFoundException | NotValidException | FoundUserPostException e) {
                    o.setErrors(e.getMessage());
                    log.error("Importing user data is failed. {}", e.getMessage());
                } finally {
                    importUsersReportRepository.updateImportUsersData(o);
                    processedUsers++;
                    if (processedUsers % 500 == 0) {
                        log.info("processedUsers " + processedUsers);
                    }
                }
            }
            importUsersReport.setCountClones(countClones.intValue());
            importUsersReport.setCountCreatedUsers(createdUsers.intValue());
            importUsersReport.setStatus(ImportUsersReportStatus.DONE);

            importUsersReportRepository.updateImportUsersReport(importUsersReport);

            createAdminEvent(OperationType.CREATE, importUsersReport, importUsersReport.getRealmId());
            log.info(String.format("importing users from file %s is done: countUsers=%s, countCreatedUsers=%s, countClones=%s ",
                    importUsersReport.getName(), importUsersReport.getCountImportUsers(), importUsersReport.getCountCreatedUsers(),
                    importUsersReport.getCountClones()));

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

    private UserEntity createUser(String realmId, ImportUsersDataEntity importUserData) {
        UserEntity user = new UserEntity();
        user.setCreatedTimestamp(System.currentTimeMillis());
        user.setUsername(importUserData.getEmail().toLowerCase());
        user.setEmail(importUserData.getEmail().toLowerCase(), false);
        user.setFirstName(importUserData.getFirstName());
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
        attributeEntity.setValue(importUserData.getPhone());
        userRepository.saveAttributes(attributeEntity);

        return user;
    }

    private void createAdminEvent(OperationType operationType, ImportUsersReportEntity report, String realmId) {
        AdminEventEntity adminEvent = new AdminEventEntity();
        adminEvent.setTime(Time.toMillis(Time.currentTime()));
        adminEvent.setRealmId(realmId);
        adminEvent.setOperationType(operationType.name());
        adminEvent.setAuthRealmId(realmId);
        adminEvent.setResourcePath("schedule/importUsersReport/" + report.getId());
        adminEvent.setResourceType("USER");
        adminEventRepository.save(adminEvent);
    }

    private void addUserPost(UserEntity user, ImportUsersDataEntity userImport) throws NotFoundException, FoundUserPostException, NotValidException {
        UserPostRequest userPostRequest = new UserPostRequest();
        userPostRequest.setUserId(user.getId());
        userPostRequest.setTomsId(userImport.getTomsId());
        userPostRequest.setDmpId(userImport.getDmpId());

        userPostRequest.setRoleId(userPostService.getUserPostRole(userImport.getRole()));
        UserPostResponse userPostResponse = userPostService.save(userPostRequest);

        addSystemRoles(userImport, userPostResponse.getId());
    }

    private void addSystemRoles(ImportUsersDataEntity userImport, String userPostId) throws javassist.NotFoundException {
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
