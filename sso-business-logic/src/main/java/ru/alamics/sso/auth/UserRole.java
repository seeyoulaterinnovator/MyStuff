package ru.alamics.sso.auth;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import ru.alamics.sso.keycloak.repository.RoleRepository;
import ru.alamics.sso.keycloak.repository.UserPostRepository;
import ru.alamics.sso.keycloak.repository.UserRepository;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;

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

    public void setUserPost (AuthenticationFlowContext context) {
        final String DEBUG_STR = "setUserPost";
        log.debug("{}: user={}", DEBUG_STR, context.getUser().getId());
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        final String tomsId = formData.getFirst("tomsId");
        final String roleName = formData.get("roleName").get(0);

        var user = context.getUser();
        var userPost = postRepository.find(user.getId(), tomsId, roleName);
        userPost.setSelected(true);
        postRepository.getAllUserPost()
                .stream()
                .filter(o ->
                        o.getUser().getId().equals(userPost.getUser().getId()) &&
                                !o.getId().equals(userPost.getId()))
                .forEach(o -> {
                    o.setSelected(false);
                    postRepository.update(o);
                });


//        var realm = context.getRealm();
//        var roleEntity = repository.findRoleEntity(roleName, realm.getId());
//
//        if (roleEntity == null) {
//            var realmEntity = new RealmEntity();
//            realmEntity.setId(realm.getId());
//            roleEntity = new RoleEntity();
//            roleEntity.setName(roleName);
//            roleEntity.setRealm(realmEntity);
//            roleEntity.setRealmId(realm.getId());
//            roleEntity.setClientRole(false);
//            roleEntity = repository.save(roleEntity);
//        }
//
//        UserEntity userEntity = userRepository.findUser(user.getId());
//        Set<UserPostEntity> userPosts = postRepository.findUserPostRole(userEntity);
//        repository.deleteUserPostRoles(userEntity, userPosts, realm.getId());
//
//        Set<ExternalSystemRoleEntity> externalSystemRoleEntities = postRepository.findSystemByUser(userEntity);
//        repository.deleteUserSystemPostClientRoles(userEntity, externalSystemRoleEntities, realm.getId());
//
//        UserRoleMappingEntity mappingEntity = new UserRoleMappingEntity();
//        mappingEntity.setRoleId(roleEntity.getId());
//        mappingEntity.setUser(userEntity);
//        repository.save(mappingEntity);
//
//        //FIXME Добавить роли пользователя по его системам, сделать можно лучше
//        List<ExternalSystemRoleEntity> systems = postRepository.findSystemsByUserPost(userPost);
//        systems.forEach(system -> {
//            var externalSystem = system.getExternalSystem();
//            var client = repository.findClientByName(externalSystem.getName(), realm.getId());
//            if(client != null) {
//                var role = repository.findClientRoleEntity(system.getName(), realm.getId(), client);
//                if(role == null) {
//                    var realmEntity = new RealmEntity();
//                    realmEntity.setId(realm.getId());
//                    role = new RoleEntity();
//                    role.setName(system.getName());
//                    role.setRealm(realmEntity);
//                    role.setRealmId(realm.getId());
//                    role.setClientRole(true);
//                    role.setClient(client);
//                    role.setClientRealmConstraint(client.getId());
//                    role = repository.save(role);
//                }
//
//                UserRoleMappingEntity roleMapping = new UserRoleMappingEntity();
//                roleMapping.setRoleId(role.getId());
//                roleMapping.setUser(userEntity);
//                repository.save(roleMapping);
//            }
//        });

        user.setAttribute(ATTR_TOMS_NAME, Collections.singletonList(userPost.getTomsId()));
    }
}
