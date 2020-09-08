package ru.alamics.sso.user;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.Time;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.jpa.AdminEventEntity;
import org.keycloak.models.jpa.entities.*;
import ru.alamics.sso.jpa.entity.ImportUsersDataEntity;
import ru.alamics.sso.jpa.entity.ImportUsersReportEntity;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.jpa.repository.*;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.ExternalSystemRoleDto;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.util.Util;
import ru.alamics.sso.util.validator.*;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;

@Slf4j
@Stateless
@LocalBean
public class MigrationService {

    private final static Long DEFAULT_ROLE_ID = 1L;   //Соответствует роли LPR // TODO но это не точно
    private final static String DEFAULT_ROLE_STR = "LPR";

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

    public void createImportUsers(ImportUsersReportEntity importUsersReport) {

        log.info("importing users from file {} in progress", importUsersReport.getName());
        long migrationStarts = new Date().getTime();

        AtomicInteger createdUsers = new AtomicInteger();
        AtomicInteger countClones = new AtomicInteger();

        for (ImportUsersDataEntity o : importUsersReport.getImportUserData()) {
            UserEntity user = null;
            boolean modified = false;
            try {
                o.setEmail(UserServiceUtil.doCleanMail(o.getEmail()));
                o.setPhone(UserServiceUtil.doCleanPhone(o.getPhone()));

                o.setErrors(null);

                user = checkImportUser(importUsersReport.getRealmId(), o.getEmail(), o.getPhone());
                if (user == null) {
                    user = createUser(importUsersReport.getRealmId(), o);
                    createdUsers.getAndIncrement();
                    o.setCreated(true);
                    modified = true;
                }
                o.setUserId(user.getId());

                checkToms(o);
                boolean modif = addUserPost(user, o);
                if (modif) {
                    modified = true;
                }

            } catch (AllNotValidException av) {

                o.setErrors(av.getMessageList().toString());
                log.error("Importing user data is failed. {}", av.getMessageList().toString());

            } catch (NotFoundException | NotValidException e) {
                o.setErrors(e.getMessage());
                log.error("Importing user data is failed. {}", e.getMessage());
            } finally {

                if (user != null && modified) {
                    addMigrationAttribute(importUsersReport.getId(), user, migrationStarts);
                } else if (!modified) {
                    countClones.incrementAndGet();
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
    }

    private void addMigrationAttribute(String reportId, UserEntity user, long migrationStarts) {

        UserAttributeEntity attributeEntity = new UserAttributeEntity();
        attributeEntity.setId(UUID.randomUUID().toString());
        attributeEntity.setName("migration" + reportId);
        attributeEntity.setUser(user);
        attributeEntity.setValue(String.valueOf(migrationStarts));
        userRepository.saveAttributes(attributeEntity);
    }

    private UserEntity checkImportUser(String realmId, String email, String phone) throws AllNotValidException {

        ValidatorBuilder vb = new ValidatorBuilder().setEmail(email).setPhone(phone).build();
        StringValidator.process(vb);

        UserEntity user = getUserByPhone(phone);
        if (user != null)
            return user;

        return getUserByEmailAndUsername(realmId, email);
    }

    private void checkToms(ImportUsersDataEntity o) throws NotFoundException {

        if (o.getTomsId() == null || o.getTomsId().isEmpty()) {
            throw new NotFoundException("TomsId is not exist");
        }
    }

    private UserEntity getUserByPhone(String phone) {
        return userRepository.getFirstUserByPhone(phone);
    }

    private UserEntity getUserByEmailAndUsername(String realmId, String email) {

        // Double-check duplicated username and email here due to federation
        UserEntity user = userRepository.getFirstUserByEmail(realmId, email);
        if (user != null) {
            return user;
        }

        return userRepository.getFirstUserByUsername(realmId, email);
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
        if (importUserData.getCleanPassword() != null) {
            user.setEmailVerified(true);
            user.setEnabled(true);
        }

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
        adminEvent.setResourcePath("migration/importUsersReport/" + report.getId());
        adminEvent.setResourceType("USER");
        adminEventRepository.save(adminEvent);
    }

    private boolean addUserPost(UserEntity user, ImportUsersDataEntity userImport) throws NotFoundException, NotValidException {

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

        userPostService.addAllSystemRole(postId);

        if (modified) {
            userImport.setRole(DEFAULT_ROLE_STR);
            userImport.setSystems(Util.join(userPostService.getAllExternalSystemLabels(), ","));
        }

        return modified;
    }
}
