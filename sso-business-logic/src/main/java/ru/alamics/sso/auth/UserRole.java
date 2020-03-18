package ru.alamics.sso.auth;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.*;
import ru.alamics.sso.jpa.entity.ExternalSystemEntity;
import ru.alamics.sso.jpa.entity.ExternalSystemRoleEntity;
import ru.alamics.sso.jpa.entity.UserPostEntity;
import ru.alamics.sso.jpa.entity.UserPostRoleEntity;
import ru.alamics.sso.jpa.repository.RoleRepository;
import ru.alamics.sso.jpa.repository.UserPostRepository;
import ru.alamics.sso.jpa.repository.UserRepository;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_TOMS_NAME;

@Stateless(name = "UserRole")
@Slf4j
@LocalBean
public class UserRole {

    @EJB
    private RoleRepository roleRepository;
    @EJB
    private UserRepository userRepository;
    @EJB
    private UserPostRepository postRepository;

    public void setUserPost(AuthenticationFlowContext context) {
        final String DEBUG_STR = "setUserPost";
        log.debug("{}: user={}", DEBUG_STR, context.getUser().getId());

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        final String tomsId = formData.getFirst("tomsId");
        final String selectedPostId = formData.get("postId").get(0);

        UserModel user = context.getUser();
        UserEntity userEntity = userRepository.findUser(user.getId());

        deselectAllPostsByUser(userEntity);

        selectPostByUser(userEntity, selectedPostId);

        user.setAttribute(ATTR_TOMS_NAME, Collections.singletonList(tomsId));
    }

    private void deselectAllPostsByUser(UserEntity user) {
        List<UserPostEntity> userPosts = postRepository.getAllUserPostByUserId(user.getId());

        userPosts.forEach(post -> post.setSelected(false));

        unbindAllRolesToUser(user);
    }

    private void selectPostByUser(UserEntity user, String selectedPostId) {
        UserPostEntity post = postRepository.getUserPost(selectedPostId);

        post.setSelected(true);

        bindRolesToUser(user, post);
    }

    private void unbindAllRolesToUser(UserEntity user) {
        Set<String> postRoleNames = new HashSet<>();

        postRoleNames.addAll(postRepository.getAllUserPostRoles().stream()
                .map(UserPostRoleEntity::getName).collect(Collectors.toSet()));

        postRoleNames.addAll(postRepository.getAllExternalSystemRole().stream()
                .map(ExternalSystemRoleEntity::getName).collect(Collectors.toSet()));

        roleRepository.unbindRolesToUserByNames(user, postRoleNames);
    }

    private void bindRolesToUser(UserEntity user, UserPostEntity post) {
        bindRoleToUserByName(user, post.getRole().getName());

        bindClientRolesToUserBySystemRoles(user, post.getSystemRoles());
    }

    private void bindRoleToUserByName(UserEntity user, String roleName) {
        RoleEntity roleEntity = roleRepository.findRoleEntityByName(roleName, user.getRealmId());
        if (roleEntity == null) {
            roleEntity = createRole(user.getRealmId(), roleName);
        }

        UserRoleMappingEntity mappingEntity = new UserRoleMappingEntity();
        mappingEntity.setRoleId(roleEntity.getId());
        mappingEntity.setUser(user);
        roleRepository.save(mappingEntity);
    }

    private void bindClientRolesToUserBySystemRoles(UserEntity user, Set<ExternalSystemRoleEntity> systemRoles) {
        systemRoles.forEach(sysRole -> {
            ExternalSystemEntity system = sysRole.getExternalSystem();

            ClientEntity client = roleRepository.findClientByName(system.getName(), user.getRealmId());

            if (client == null) {
                return;
            }

            RoleEntity role = roleRepository.findClientRoleEntity(sysRole.getName(), user.getRealmId(), client);
            if (role == null) {
                role = createClientRole(user.getRealmId(), sysRole.getName(), client);
            }

            UserRoleMappingEntity roleMapping = new UserRoleMappingEntity();
            roleMapping.setRoleId(role.getId());
            roleMapping.setUser(user);
            roleRepository.save(roleMapping);
        });
    }

    private RoleEntity createClientRole(String realmId, String roleName, ClientEntity client) {
        RoleEntity clientRole = createRoleEntity(realmId, roleName);

        clientRole.setClientRole(true);
        clientRole.setClient(client);
        clientRole.setClientRealmConstraint(client.getClientId());
        return roleRepository.save(clientRole);
    }

    private RoleEntity createRole(String realmId, String roleName) {
        RoleEntity roleEntity = createRoleEntity(realmId, roleName);

        roleEntity.setClientRole(false);

        return roleRepository.save(roleEntity);
    }

    private RoleEntity createRoleEntity(String realmId, String roleName) {
        RoleEntity roleEntity = new RoleEntity();

        roleEntity.setName(roleName);

        RealmEntity realmEntity = new RealmEntity();
        realmEntity.setId(realmId);
        roleEntity.setRealm(realmEntity);

        roleEntity.setRealmId(realmId);

        return roleEntity;
    }
}
