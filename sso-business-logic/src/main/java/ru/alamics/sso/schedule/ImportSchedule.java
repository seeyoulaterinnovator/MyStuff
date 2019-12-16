package ru.alamics.sso.schedule;


import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.Time;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.jpa.AdminEventEntity;
import org.keycloak.models.jpa.entities.*;
import ru.alamics.sso.keycloak.entity.ImportUsersDataEntity;
import ru.alamics.sso.keycloak.entity.ImportUsersReportEntity;
import ru.alamics.sso.keycloak.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.keycloak.repository.*;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.util.Util;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.validation.ValidationException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Startup
@Singleton
@DependsOn("ApplicationProperties")
public class ImportSchedule {
    private static final String TIMER_NAME = "Import Schedule Timer";
    private static final long DEFAULT_INTERVAL_DURATION = 60000;

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
    private ApplicationProperties properties;
    @Resource
    private TimerService timerService;

    @PostConstruct
    private void init() {
        final TimerConfig timerConfig = new TimerConfig(TIMER_NAME, false);
        try {
            final long intervalDuration = Long.parseLong(properties.getProperty("application.schedule.import.milliseconds"));
            timerService.createIntervalTimer(intervalDuration, intervalDuration, timerConfig);
            log.info("Timer:{} is created, interval duration set to value={} milliseconds ", TIMER_NAME, intervalDuration);
        } catch (Exception e) {
            timerService.createIntervalTimer(DEFAULT_INTERVAL_DURATION, DEFAULT_INTERVAL_DURATION, timerConfig);
            log.warn("Timer:{} is created; Error read configuration, interval duration set to default value={} milliseconds",
                    TIMER_NAME, DEFAULT_INTERVAL_DURATION);
        }
    }

    @Timeout
    public void schedule(Timer timer) {
        if (!TIMER_NAME.equals(timer.getInfo().toString())) {
            return;
        }

        log.info("Schedule by timer:{}", timer.getInfo());
        log.info("Start import users by schedule");
        List<ImportUsersReportEntity> importUsersReportEntities = importUsersReportRepository.findAllImportUsersReports()
                .stream()
                .filter(o -> o.getImportUserData() != null && !o.getImportUserData().isEmpty())
                .filter(o -> o.getStatus().equals(ImportUsersReportStatus.AWAITING))
                .peek(o -> {
                    o.setStatus(ImportUsersReportStatus.IN_PROGRESS);
                    importUsersReportRepository.updateImportUsersReport(o);
                })
                .collect(Collectors.toList());
        for (ImportUsersReportEntity importUsersReportEntity : importUsersReportEntities) {
            createImportUsers(importUsersReportEntity);
        }
        log.info("End import users by schedule");
    }

    private void createImportUsers(ImportUsersReportEntity importUsersReport) {
        log.info("importing users from file {} in progress", importUsersReport.getName());
        AtomicInteger createdUsers = new AtomicInteger();
        AtomicInteger countClones = new AtomicInteger();
        for (ImportUsersDataEntity o : importUsersReport.getImportUserData()) {
            try {
                o.setErrors(null);
                checkImportUser(importUsersReport.getRealmId(), o.getEmail(), o.getPhone());
                UserEntity user = createUser(importUsersReport.getRealmId(), o);
                createdUsers.getAndIncrement();
                o.setCreated(true);
                o.setUserId(user.getId());
                if (o.getTomsId() == null || o.getTomsId().isBlank()) {
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
            } catch (NotFoundException | ValidationException e) {
                o.setErrors(e.getMessage());
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

    private void checkImportUser(String realmId, String email, String phone) throws FoundException {
        Util.validateUserPhoneAndEmail(email, phone);

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
        attributeEntity.setName("phone");
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

    private void addUserPost(UserEntity user, ImportUsersDataEntity userImport) throws javassist.NotFoundException {
        UserPostRequest userPostRequest = new UserPostRequest();
        userPostRequest.setUserId(user.getId());
        userPostRequest.setTomsId(userImport.getTomsId());
        userPostRequest.setDmpId(userImport.getDmpId());

        userPostRequest.setRoleId(userPostService.getUserPostRole(userImport.getRole()));
        UserPostResponse userPostResponse = userPostService.save(userPostRequest);

        addSystemRoles(userImport, userPostResponse.getId());
    }

    private void addSystemRoles(ImportUsersDataEntity userImport, String userPostId) throws javassist.NotFoundException {
        List<String> systems = List.of(userImport.getSystems().replaceAll("\\s", "").split(","));
        if (systems != null && !systems.isEmpty()) {
            for (String sysName : systems) {
                userPostService.addSystemRole(UserMapper.toExternalSystemRoleRequest(userPostId,
                        userPostService.getExternalSystemRoleId(sysName)));
            }
        }
    }
}
