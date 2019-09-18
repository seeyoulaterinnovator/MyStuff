package ru.alamics.sso.auth;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.jpa.entities.*;
import ru.alamics.sso.keycloak.entity.UserPostEntity;
import ru.alamics.sso.keycloak.repository.RoleRepository;
import ru.alamics.sso.keycloak.repository.UserPostRepository;
import ru.alamics.sso.keycloak.repository.UserRepository;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

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

    public void roleSetting (AuthenticationFlowContext context) {
        final String DEBUG_STR = "roleSetting";
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        final String roleName = formData.get("roleName").get(0);
        var realm = context.getRealm();
        var roleEntity = repository.findRoleEntity(roleName, realm.getId());
        var user = context.getUser();
        if (roleEntity == null) {
            var realmEntity = new RealmEntity();
            roleEntity = new RoleEntity();
            roleEntity.setName(roleName);
            realmEntity.setId(realm.getId());
            roleEntity.setRealm(realmEntity);
            roleEntity.setRealmId(realm.getId());
            roleEntity.setClientRole(false);
            roleEntity = repository.save(roleEntity);
        }
        UserEntity userEntity = userRepository.findUser(user.getId());
        List<UserPostEntity> userPosts = postRepository.findUserPostRole(userEntity);
        repository.deleteUserPostRoles(userEntity, userPosts, realm.getId());
        UserRoleMappingEntity mappingEntity = new UserRoleMappingEntity();
        mappingEntity.setRoleId(roleEntity.getId());
        mappingEntity.setUser(userEntity);
        repository.save(mappingEntity);
    }
}
