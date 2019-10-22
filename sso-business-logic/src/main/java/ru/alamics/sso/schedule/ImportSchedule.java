package ru.alamics.sso.schedule;


import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.*;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.models.jpa.entities.*;
import org.keycloak.models.utils.DefaultRoles;
import ru.alamics.sso.keycloak.entity.ImportUserDataEntity;
import ru.alamics.sso.keycloak.entity.ImportUserHistoryEntity;
import ru.alamics.sso.keycloak.repository.*;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.user.model.ImportResponse;
import ru.alamics.sso.util.Util;

import javax.ejb.*;
import javax.validation.ValidationException;
import javax.ws.rs.core.Context;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;

@Slf4j
@Singleton
@Startup
public class ImportSchedule {
    @EJB
    private ImportUserHistoryRepository importUserHistoryRepository;
    @EJB
    private UserRepository userRepository;
    @EJB
    private RealmRepository realmRepository;
    @EJB
    private RoleRepository roleRepository;
    @EJB
    private UserAttributeRepository userAttributeRepository;
    @Context
    private KeycloakSession session;

    @Schedule(hour = "*", minute = "*/1", persistent = false)
    public void schedule() {
        importUserHistoryRepository.findAllImportUserHistoryEntitiesIsDone()
                .stream()
                .filter(o -> o.getImportUserData() != null && !o.getImportUserData().isEmpty())
                .forEach(o -> createImportUsers(o));
    }

    private void createImportUsers(ImportUserHistoryEntity importUserHistory) {
        ImportResponse importResponse = new ImportResponse();

        AtomicInteger createdUsers = new AtomicInteger();
        AtomicInteger countClones = new AtomicInteger();
        importUserHistory.getImportUserData().stream()
                .forEach(o -> {
                    try {
                        checkImportUser(importUserHistory.getRealmId(), o.getEmail(), o.getPhone());
                        UserEntity user = createUser(importUserHistory.getRealmId(), o);
//                        createAdminEvent(OperationType.CREATE, user);
//                        createdUsers.getAndIncrement();
//                        o.setCreated(true);
//                        importResponse.addCreatedUserIds("userId", user.getId());
//
//                        if (userRequest.getTomsId() == null || userRequest.getTomsId().isBlank()) {
//                            throw new NotFoundException("TomsId is not exist");
//                        }
//                        addUserPost(user, o, userRequest);
                    } catch (FoundException e) {
                        e.getResult().forEach((k, v) -> {
                            Map<String, Object> error = new HashMap<>();
                            error.put("error", v);
                            error.put("importUserName", o.getFirstName());
                            importResponse.addError(error);
                        });
                        countClones.getAndIncrement();
                    } catch (ValidationException e) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("error", e.getMessage());
                        error.put("importUserName", o.getFirstName());
                        importResponse.addError(error);
                    }
                });
        importResponse.setCreatedUsers(createdUsers);
        importResponse.setCountClones(countClones);
    }

    private void checkImportUser(String realmId, String email, String phone) throws FoundException {
        Util.validateUserPhoneAndEmail(email, phone);

        FoundException foundException = new FoundException();
        try {
            checkOnExistUserByPhone(phone, realmId);
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

    private void checkOnExistUserByPhone(String phone, String realm) throws FoundException {
        UserEntity user = userRepository.getFirstUserByPhone(realm, phone);

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

    private UserEntity createUser(String realmId, ImportUserDataEntity importUserData) {
        UserEntity user = new UserEntity();
        user.setCreatedTimestamp(System.currentTimeMillis());
        user.setUsername(importUserData.getEmail().toLowerCase());
        user.setEmail(importUserData.getEmail().toLowerCase(), false);
        user.setFirstName(importUserData.getFirstName());
        user.setRealmId(realmId);
        user.setEmailVerified(false);
        user.setEnabled(true);
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
        userAttributeRepository.save(attributeEntity);
        return user;
    }
}
