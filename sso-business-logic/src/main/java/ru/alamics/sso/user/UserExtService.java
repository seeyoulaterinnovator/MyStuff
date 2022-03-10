package ru.alamics.sso.user;

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
import ru.alamics.sso.jpa.entity.UserPostRoleEntity;
import ru.alamics.sso.keycloak.facade.UserPostFacade;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.model.UserRequest;
import ru.alamics.sso.util.Util;
import ru.alamics.sso.util.validator.NotValidException;

import javax.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;

/**
 * creates user from external sources
 * <p>
 * same as ImportService
 */
@Slf4j
public class UserExtService {

    private final static Long DEFAULT_ROLE_ID = 1L;   //Соответствует роли LPR, но это не точно

    private final AdminAuth auth;
    private final KeycloakSession session;
    private final RealmModel realm;

    private final UserFindService userFindService;
    private final UserPostFacade userPostFacade;

    public UserExtService(KeycloakSession session, AdminAuth auth) {

        this.auth = auth;
        this.session = session;
        this.realm = session.getContext().getRealm();

        this.userFindService = Lookup.lookup(UserFindService.class);
        this.userPostFacade = Lookup.lookup(UserPostFacade.class);
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

        user.setEmailVerified(false);
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

        String phone = Util.getCleanUserPhone(request.getPhone());
        if (phone != null)
            user.setAttribute(ATTR_PHONE_NAME, Collections.singletonList(phone));
    }

    private void commit() {
        if (session.getTransactionManager().isActive()) {
            session.getTransactionManager().commit();
        }
    }

    private synchronized UserModel createUser(UserRequest userRequest) {
        try {
            userRequest.setPhone(Util.getCleanUserPhone(userRequest.getPhone()));

            UserModel user = session.users().addUser(realm, userRequest.getEmail());
            updateUserFromRequest(user, userRequest, realm, session, false);
            return user;
        } finally {
            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().setRollbackOnly();
            }
        }
    }

    public UserModel createUser(UserRequest request, boolean bss) throws FoundException, NotFoundException, FoundUserPostException, NotValidException {

        request.setEmail(UserServiceUtil.doCleanMail(request.getEmail()));
        request.setPhone(UserServiceUtil.doCleanPhone(request.getPhone()));

        FoundException exception = null;

        String userIdByPhone = null;
        try {
            checkOnExistUserByPhone(request, realm);
        } catch (FoundException e) {

            if (!bss)
                throw e;

            exception = e;
            userIdByPhone = (String) e.getResult().get("userId");
        }

        String userIdByEmail = null;
        try {
            checkOnExistUserByEmailAndUsername(request, realm);
        } catch (FoundException e) {

            if (!bss)
                throw e;

            exception = e;
            userIdByEmail = (String) e.getResult().get("userId");
        }

        if (bss) {
            // xor - нашли совпадение только по одному
            if ((userIdByPhone != null) ^ (userIdByEmail != null)) {
                throw exception;
            }

            // нашли юзеров по телефону и по мылу, но их ид - разные
            if ((userIdByPhone != null) && (userIdByEmail != null)) {

                if (!userIdByPhone.equals(userIdByEmail)) {
                    throw exception;
                }
            }
        }


        boolean userNotFound = userIdByPhone == null && userIdByEmail == null;

        UserModel user = null;
        if (bss && !userNotFound) {

            user = session.users().getUserById(userIdByPhone, realm);

        } else {
            user = createUser(request);
        }


        if (!Util.isEmpty(request.getTomsId())) {
            addUserPostRole(user, request, bss);
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
            throw new FoundException("Уже существует УЗ с таким phone").addResult("userId", user.getId());
        }
    }

    private void checkOnExistUserByEmailAndUsername(UserRequest request, RealmModel realm) throws FoundException {
        // Double-check duplicated username and email here due to federation
        if (request.getEmail() != null && !realm.isDuplicateEmailsAllowed()) {
            UserModel userModel = session.users().getUserByEmail(request.getEmail(), realm);
            if (userModel != null) {
                log.error("User exists with same email {}", request.getEmail());
                throw new FoundException("Уже существует УЗ с таким email").addResult("userId", userModel.getId());
            }
        }

        UserModel userModel = session.users().getUserByUsername(request.getEmail(), realm);
        if (userModel != null) {
            log.error("User exists with same username {}", request.getEmail());
            throw new FoundException("Уже существует УЗ с таким username").addResult("userId", userModel.getId());
        }
    }

    private void createAdminEvent(OperationType operationType, UserModel user) {
        new AdminEventBuilder(realm, auth, session, session.getContext().getConnection())
                .realm(realm)
                .resource(ResourceType.USER)
                .operation(operationType)
                .resourcePath(session.getContext().getUri(), user.getId())
                .success();
    }

    private void addUserPostRole(UserModel userModel, UserRequest request, boolean bss) throws NotFoundException, FoundException, FoundUserPostException, NotValidException {

        UserPostRequest userPostRequest = UserMapper.toUserPostRequest(userModel, request);

        if (bss || request.getRoleId() == null) {
            userPostRequest.setRoleId(DEFAULT_ROLE_ID);
        }
        if (request.getRoleId() != null && !bss) {
            UserPostRoleEntity role = userFindService.getRoleEntity(request.getRoleId());
            if (role == null) {
                throw new NotFoundException("Роль не найдена.");
            }
            userPostRequest.setRoleId(request.getRoleId());
        }

        UserPostResponse userPostResponse = userPostFacade.save(userPostRequest);

        userPostFacade.getUserPostService().addAllSystemRole(userPostResponse.getId());
    }
}
