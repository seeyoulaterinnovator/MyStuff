package ru.alamics.sso.user;

import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.common.util.Time;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.jpa.AdminEventEntity;
import org.keycloak.models.jpa.entities.*;
import ru.alamics.sso.jpa.entity.common.ImportUsersDataStatus;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.jpa.repository.*;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.schedule.ImportSchedule;
import ru.alamics.sso.user.model.ImportUsersDataModel;
import ru.alamics.sso.user.model.ImportUsersReportModel;
import ru.alamics.sso.user.model.RepeatNextTimeException;
import ru.alamics.sso.util.Util;
import ru.alamics.sso.util.validator.AllNotValidException;
import ru.alamics.sso.util.validator.NotValidException;
import ru.alamics.sso.util.validator.StringValidator;
import ru.alamics.sso.util.validator.ValidatorBuilder;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;

@ApplicationScoped
@Slf4j
public class MigrationService {

    private final static Long DEFAULT_ROLE_ID = 1L;   //Соответствует роли LPR, но это не точно
    private final static String DEFAULT_ROLE_STR = "LPR";

    @Inject
    ImportUsersReportRepository importUsersReportRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    RealmRepository realmRepository;
    @Inject
    RoleRepository roleRepository;
    @Inject
    AdminEventRepository adminEventRepository;
    @Inject
    UserPostService userPostService;
    @Inject
    ImportReportService importReportService;
    @Inject
    PersonalAccountService personalAccountService;

    public List<UserEntity> createImportUsers(ImportUsersReportModel reportModel, List<ImportUsersDataModel> dataList, Long scheduleStart) {

        log.info("importing users from file {} in progress", reportModel.getName());
        long migrationStarts = new Date().getTime();

        List<UserEntity> entities = new ArrayList<>();

        AtomicInteger createdUsers = new AtomicInteger(reportModel.getCountCreatedUsers());
        AtomicInteger countClones = new AtomicInteger(reportModel.getCountClones());
        AtomicInteger processedUsers = new AtomicInteger();

        try {
            for (ImportUsersDataModel data : dataList) {

                ImportSchedule.checkTimeout(scheduleStart);

                if (data.getStatus() == ImportUsersDataStatus.DONE)
                    continue;

                QuarkusTransaction.requiringNew().run(() -> {
                    UserEntity user = null;
                    boolean modified = false;

                    try {
                        data.setEmail(UserServiceUtil.doCleanMail(data.getEmail()));
                        data.setPhone(UserServiceUtil.doCleanPhone(data.getPhone()));

                        data.setErrors(null);

                        user = checkImportUser(reportModel.getRealmId(), data.getEmail(), data.getPhone());
                        if (user == null) {
                            user = createUser(reportModel.getRealmId(), data);

                            data.setCreated(true);
                            modified = true;
                        } else {
                            user.setFirstName(data.getFirstName());
                        }
                        data.setUserId(user.getId());
                        checkToms(data);

                        modified = addUserPost(user, data);

                        entities.add(user);

                    } catch (AllNotValidException av) {

                        data.setErrors(av.getMessageList().toString());
                        log.error("Importing user data is failed. {}", av.getMessageList().toString());

                    } catch (NotFoundException | NotValidException e) {
                        data.setErrors(e.getMessage());
                        log.error("Importing user data is failed. {}", e.getMessage());
                    } catch (FoundException e) {
                        List<Object> errors = new LinkedList<>();
                        e.getResult().forEach((k, v) -> {
                            errors.add(v);
                        });
                        String errorsStr = errors.toString().substring(1, errors.toString().length() - 1);
                        data.setErrors(errorsStr);
                        log.error("Importing user data is failed. {}", errorsStr);
                    } finally {

                        if (user != null && modified) {
                            addMigrationAttribute(reportModel.getId(), user, migrationStarts);
                            createdUsers.incrementAndGet();
                        } else if (!modified) {
                            countClones.incrementAndGet();
                        }

                        data.setStatus(ImportUsersDataStatus.DONE);

                        importReportService.updateImportUsersData(data);
                        processedUsers.incrementAndGet();
                        if (processedUsers.get() % 50 == 0) {
                            reportModel.setCountClones(countClones.get());
                            reportModel.setCountCreatedUsers(createdUsers.get());
                            importReportService.updateReport(reportModel);
                            log.info("ProcessedUsers " + processedUsers);
                        }
                    }
                });
            }

            reportModel.setCountClones(countClones.get());
            reportModel.setCountCreatedUsers(createdUsers.get());
            reportModel.setStatus(ImportUsersReportStatus.DONE);

            // закомментировано, потому что для пакетной загрузки это может быть не окончательный статус
            //importReportService.updateReport(reportModel);

            createAdminEvent(OperationType.CREATE, reportModel, reportModel.getRealmId());

            log.info(String.format("importing users from file %s is done: countUsers=%s, countCreatedUsers=%s, countClones=%s ",
                    reportModel.getName(), reportModel.getCountImportUsers(), reportModel.getCountCreatedUsers(),
                    reportModel.getCountClones()));
        } catch (RepeatNextTimeException rte) {

            log.info("Interrupted by timeout, processed " + processedUsers);

            reportModel.setCountClones(countClones.get());
            reportModel.setCountCreatedUsers(createdUsers.get());
            reportModel.setStatus(ImportUsersReportStatus.AWAITING);
            // закомментировано, потому что для пакетной загрузки это может быть не окончательный статус
            //importReportService.updateReport(reportModel);

        } catch (Exception e) {
            log.error("Error, but processed " + processedUsers, e);
            throw e;
        }
        return entities;
    }

    private void addMigrationAttribute(String reportId, UserEntity user, long migrationStarts) {

        UserAttributeEntity attributeEntity = new UserAttributeEntity();
        attributeEntity.setId(UUID.randomUUID().toString());
        attributeEntity.setName("migration" + reportId);
        attributeEntity.setUser(user);
        attributeEntity.setValue(String.valueOf(migrationStarts));
        userRepository.saveAttributes(attributeEntity);
    }

    private UserEntity checkImportUser(String realmId, String email, String phone) throws AllNotValidException, FoundException {

        ValidatorBuilder vb = new ValidatorBuilder().setEmail(email).setPhone(phone).build();
        StringValidator.process(vb);

        UserEntity byPhone = getUserByPhone(realmId, phone);
        UserEntity byEmail = getUserByEmailAndUsername(realmId, email);

        if (byPhone != null && byPhone.equals(byEmail)) {
            return byPhone;
        }

        FoundException foundException = new FoundException();
        if (byPhone == null && byEmail != null) {
            foundException.addResult("error1", "User exists with same email userId " + byEmail.getId());
        } else if (byPhone != null && byEmail == null) {
            foundException.addResult("error2", "User exists with same phone userId " + byPhone.getId());
        } else if (byEmail != null) {
            foundException.addResult("error1", "User exists with same email userId " + byEmail.getId());
            foundException.addResult("error2", "User exists with same phone userId " + byPhone.getId());
        }
        if (foundException.getResult() != null) {
            throw foundException;
        }

        return null;
    }

    private void checkToms(ImportUsersDataModel o) throws NotFoundException {

        if (o.getTomsId() == null || o.getTomsId().isEmpty()) {
            throw new NotFoundException("TomsId is not exist");
        }
    }

    private UserEntity getUserByPhone(String realmId, String phone) {
        return userRepository.getFirstUserByPhoneNumber(realmId, phone, null);
    }

    private UserEntity getUserByEmailAndUsername(String realmId, String email) {

        // Double-check duplicated username and email here due to federation
        UserEntity user = userRepository.getFirstUserByEmail(realmId, email);
        if (user != null) {
            return user;
        }

        return userRepository.getFirstUserByUsername(realmId, email);
    }

    private UserEntity createUser(String realmId, ImportUsersDataModel importUserData) {

        UserEntity user = new UserEntity();
        user.setCreatedTimestamp(System.currentTimeMillis());
        user.setUsername(importUserData.getEmail().toLowerCase());
        user.setEmail(importUserData.getEmail().toLowerCase(), false);
        user.setFirstName(importUserData.getFirstName());
        user.setRealmId(realmId);
        user.setEmailVerified(false);
        user.setEnabled(false);
        if (importUserData.getCleanPassword() != null) {
            user.setEmailVerified(true);
            user.setEnabled(false);
        }

        user = userRepository.save(user);

        RealmEntity realm = realmRepository.findRealmEntityById(realmId);

        if (realm.getDefaultRoleId() != null) {
            UserRoleMappingEntity roleMapping = new UserRoleMappingEntity();
            roleMapping.setRoleId(realm.getDefaultRoleId());
            roleMapping.setUser(user);
            roleRepository.save(roleMapping);
        }
        UserAttributeEntity attributeEntity = new UserAttributeEntity();
        attributeEntity.setId(UUID.randomUUID().toString());
        attributeEntity.setName(ATTR_PHONE_NAME);
        attributeEntity.setUser(user);
        attributeEntity.setValue(importUserData.getPhone());
        userRepository.saveAttributes(attributeEntity);

        return user;
    }

    private void createAdminEvent(OperationType operationType, ImportUsersReportModel report, String realmId) {
        AdminEventEntity adminEvent = new AdminEventEntity();
        adminEvent.setTime(Time.toMillis(Time.currentTime()));
        adminEvent.setRealmId(realmId);
        adminEvent.setOperationType(operationType.name());
        adminEvent.setAuthRealmId(realmId);
        adminEvent.setResourcePath("migration/importUsersReport/" + report.getId());
        adminEvent.setResourceType("USER");
        adminEventRepository.save(adminEvent);
    }

    private boolean addUserPost(UserEntity user, ImportUsersDataModel userImport) throws NotFoundException, NotValidException {

        boolean modified = false;

        UserPostRequest userPostRequest = new UserPostRequest();
        userPostRequest.setUserId(user.getId());
        userPostRequest.setTomsId(userImport.getTomsId());
        userPostRequest.setDmpId(userImport.getDmpId());

        userPostRequest.setRoleId(DEFAULT_ROLE_ID);

        String postId = null;
        try {
            UserPostResponse userPostResponse = userPostService.save(userPostRequest);
            postId = userPostResponse.getId();
            modified = true;
        } catch (FoundUserPostException e) {
            postId = e.getPostId();
        }

        userPostService.addAllSystemRole(postId, user.getRealmId());

        if (modified) {
            userImport.setRole(DEFAULT_ROLE_STR);
            userImport.setSystems(Util.join(userPostService.getAllExternalSystemLabelsForRealm(user.getRealmId()), ","));
        }
        addPersonalAccount(postId, userImport);
        return modified;
    }

    private void addPersonalAccount(String postId, ImportUsersDataModel userImport) {
        String accountNumber = userImport.getPersonalAccountUser();
        if (StringUtils.isEmpty(accountNumber)) {
            return;
        }

        List<String> accNumList = new LinkedList<>();
        accNumList.add(accountNumber);
        personalAccountService.addAccountList(postId, accNumList);
    }
}
