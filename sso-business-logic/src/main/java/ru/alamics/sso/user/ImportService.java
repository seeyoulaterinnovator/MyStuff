package ru.alamics.sso.user;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.Time;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.events.jpa.AdminEventEntity;
import org.keycloak.models.*;
import org.keycloak.models.jpa.entities.*;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.storage.ReadOnlyException;
import ru.alamics.sso.jpa.entity.common.ImportUsersDataStatus;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.jpa.repository.*;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.schedule.ImportSchedule;
import ru.alamics.sso.user.filetype.FileFactory;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.model.ImportUsersDataModel;
import ru.alamics.sso.user.model.ImportUsersReportModel;
import ru.alamics.sso.user.model.RepeatNextTimeException;
import ru.alamics.sso.util.validator.EmailValidator;
import ru.alamics.sso.util.validator.NotValidException;
import ru.alamics.sso.util.validator.PhoneValidator;

import javax.ejb.*;
import javax.ws.rs.NotFoundException;
import java.util.*;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;

/**
 * imports users from files
 * <p>
 * same as UserExtService
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

    private void doGeneratePasswords(List<ImportUsersDataModel> dataList, AdminAuth auth, KeycloakSession session) {

        log.info("doGeneratePasswords");

        if (auth == null) {
            log.info("doGeneratePasswords auth is null");
            return;
        }

        if (session == null) {
            log.info("doGeneratePasswords session is null");
            return;
        }

        RealmModel realm = session.getContext().getRealm();

        Map<String, UserModel> listToSend = new HashMap<>();

        for (ImportUsersDataModel data : dataList) {

            if (data.getUserId() != null && data.getCleanPassword() != null) {

                String errors = "";

                UserModel user = session.users().getUserById(data.getUserId(), realm);

                try {
                    if (user == null) {
                        log.info("generate password userid = " + data.getUserId() + ", user is null");
                        errors += "Password not set";
                    } else {

                        UserCredentialModel cred = UserCredentialModel.password(data.getCleanPassword(), false);
                        session.userCredentialManager().updateCredential(realm, user, cred);
                    }

                } catch (IllegalStateException ise) {
                    log.error("", ise);
                    errors += "Resetting to N old passwords is not allowed.";
                } catch (ReadOnlyException mre) {
                    log.error("", mre);
                    errors += "Can't reset password as account is read only.";
                } catch (ModelException e) {
                    log.error("", e);
                    errors += e.getMessage();
                } finally {
                    if (!errors.isEmpty()) {
                        data.setErrors(data.getErrors() + errors);
                        importReportService.updateImportUsersData(data);
                    } else {
                        // чтобы не было дублей
                        if (user != null)
                            listToSend.put(user.getId(), user);
                    }
                }
            }
        }

        for (UserModel user : listToSend.values()) {
            createAdminEvent(OperationType.CREATE, user, realm, auth, session);
        }

        log.info("doGeneratePasswords done");
    }

    public void createAdminEvent(OperationType operationType, UserModel user, RealmModel realm, AdminAuth auth, KeycloakSession session) {
        new AdminEventBuilder(realm, auth, session, session.getContext().getConnection())
                .realm(realm)
                .resource(ResourceType.USER)
                .operation(operationType)
                .resourcePath(session.getContext().getUri(), user.getId())
                .success();
    }

    //@Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void createImportUsers(ImportUsersReportModel reportModel, List<ImportUsersDataModel> dataList, Long scheduleStart,
                                  AdminAuth auth, KeycloakSession session) {

        if (FileFactory.CTL.equalsIgnoreCase(reportModel.getFiletype())) {

            migrationService.createImportUsers(reportModel, dataList, scheduleStart);

        } else {
            importUsers(reportModel, dataList, scheduleStart);
        }

        doGeneratePasswords(dataList, auth, session);

        //if (cf != null)
        //    cf.complete("");
    }

    private void importUsers(ImportUsersReportModel reportModel, List<ImportUsersDataModel> dataList, Long scheduleStart) {
        log.info("Importing users from file {} in progress", reportModel.getName());

        int createdUsers = reportModel.getCountCreatedUsers();
        int countClones = reportModel.getCountClones();
        int processedUsers = 0;

        if (dataList == null)
            dataList = importReportService.getDataListAwaiting(reportModel.getId());

        try {
            for (ImportUsersDataModel data : dataList) {


                ImportSchedule.checkTimeout(scheduleStart);

                if (data.getStatus() == ImportUsersDataStatus.DONE)
                    continue;

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

                    data.setStatus(ImportUsersDataStatus.DONE);

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

            //importReportService.updateReport(reportModel);

            createAdminEvent(OperationType.CREATE, reportModel, reportModel.getRealmId());
            log.info(String.format("Importing users from file %s is done: countUsers=%s, countCreatedUsers=%s, countClones=%s ",
                    reportModel.getName(), reportModel.getCountImportUsers(), reportModel.getCountCreatedUsers(),
                    reportModel.getCountClones()));

        } catch (RepeatNextTimeException rte) {

            log.info("Interrupted by timeout, processed " + processedUsers);

            reportModel.setCountClones(countClones);
            reportModel.setCountCreatedUsers(createdUsers);
            reportModel.setStatus(ImportUsersReportStatus.AWAITING);
            //importReportService.updateReport(reportModel);

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
            checkOnExistUserByPhone(realmId, phone);
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

    private void checkOnExistUserByPhone(String realmId, String phone) throws FoundException {
        UserEntity user = userRepository.getFirstUserByPhoneNumber(realmId,phone,null);

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

    private void addUserPost(UserEntity user, ImportUsersDataModel data) throws FoundUserPostException, NotValidException {
        UserPostRequest userPostRequest = new UserPostRequest();
        userPostRequest.setUserId(user.getId());
        userPostRequest.setTomsId(data.getTomsId());
        userPostRequest.setDmpId(data.getDmpId());

        userPostRequest.setRoleId(userPostService.getUserPostRole(data.getRole()));
        UserPostResponse userPostResponse = userPostService.save(userPostRequest);

        addSystemRoles(data, userPostResponse.getId());
    }

    private void addSystemRoles(ImportUsersDataModel userImport, String userPostId) {
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
                throw new NotFoundException(String.format("Not found roles for systems: systems=%s", errorSystemNames));
            }
        }
    }
}
