package ru.alamics.sso.auth;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.*;
import ru.alamics.sso.jpa.entity.ExternalSystemEntity;
import ru.alamics.sso.jpa.entity.ExternalSystemRoleEntity;
import ru.alamics.sso.jpa.entity.UserPostEntity;
import ru.alamics.sso.jpa.repository.RoleRepository;
import ru.alamics.sso.jpa.repository.UserPostRepository;
import ru.alamics.sso.jpa.repository.UserRepository;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.List;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_TOMS_NAME;

@Stateless(name = "UserRole")
@Slf4j
@LocalBean
public class UserRole {

    @EJB
    private RoleRepository repository;
    @EJB
    private UserRepository userRepository;
    @EJB
    private UserPostRepository postRepository;

    public void setUserPost(AuthenticationFlowContext context) {
        final String DEBUG_STR = "setUserPost";
        log.debug("{}: user={}", DEBUG_STR, context.getUser().getId());
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        final String tomsId = formData.getFirst("tomsId");
        final String roleName = formData.get("roleName").get(0);

        UserModel user = context.getUser();

        List<UserPostEntity> userPosts = postRepository.getAllUserPostByUserId(user.getId());
        userPosts.forEach(o -> {
            o.setSelected(o.getCustomer().getId().equals(tomsId) && o.getRole().getName().equals(roleName));
        });

        RealmModel realm = context.getRealm();
        RoleEntity roleEntity = repository.findRoleEntity(roleName, realm.getId());

        if (roleEntity == null) {
            RealmEntity realmEntity = new RealmEntity();
            realmEntity.setId(realm.getId());
            roleEntity = new RoleEntity();
            roleEntity.setName(roleName);
            roleEntity.setRealm(realmEntity);
            roleEntity.setRealmId(realm.getId());
            roleEntity.setClientRole(false);
            roleEntity = repository.save(roleEntity);
        }

        UserEntity userEntity = userRepository.findUser(user.getId());
        repository.deleteUserPostRoles(userEntity, userPosts, realm.getId());

        List<ExternalSystemRoleEntity> externalSystemRoleEntities = postRepository.findSystemByUser(userEntity);
        repository.deleteUserSystemPostClientRoles(userEntity, externalSystemRoleEntities, realm.getId());

        UserRoleMappingEntity mappingEntity = new UserRoleMappingEntity();
        mappingEntity.setRoleId(roleEntity.getId());
        mappingEntity.setUser(userEntity);
        repository.save(mappingEntity);

        UserPostEntity activePost = userPosts.stream().filter(UserPostEntity::isSelected).findFirst().get();
        //FIXME Добавить роли пользователя по его системам, сделать можно лучше
        List<ExternalSystemRoleEntity> systems = postRepository.findSystemsByUserPost(activePost);
        systems.forEach(system -> {
            ExternalSystemEntity externalSystem = system.getExternalSystem();
            ClientEntity client = repository.findClientByName(externalSystem.getName(), realm.getId());
            if (client != null) {
                RoleEntity role = repository.findClientRoleEntity(system.getName(), realm.getId(), client);
                if (role == null) {
                    RealmEntity realmEntity = new RealmEntity();
                    realmEntity.setId(realm.getId());
                    role = new RoleEntity();
                    role.setName(system.getName());
                    role.setRealm(realmEntity);
                    role.setRealmId(realm.getId());
                    role.setClientRole(true);
                    role.setClient(client);
                    role.setClientRealmConstraint(client.getId());
                    role = repository.save(role);
                }

                UserRoleMappingEntity roleMapping = new UserRoleMappingEntity();
                roleMapping.setRoleId(role.getId());
                roleMapping.setUser(userEntity);
                repository.save(roleMapping);
            }
        });

        user.setAttribute(ATTR_TOMS_NAME, Collections.singletonList(activePost.getCustomer().getId()));
    }
}
