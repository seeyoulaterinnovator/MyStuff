package ru.alamics.sso.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.*;
import ru.alamics.sso.jpa.entity.ExternalSystemEntity;
import ru.alamics.sso.jpa.entity.ExternalSystemRoleEntity;
import ru.alamics.sso.jpa.entity.UserPostEntity;
import ru.alamics.sso.jpa.entity.UserPostRoleEntity;
import ru.alamics.sso.jpa.repository.RoleRepository;
import ru.alamics.sso.jpa.repository.UserPostRepository;
import ru.alamics.sso.jpa.repository.UserRepository;
import ru.alamics.sso.registration.dto.UserPostResponse;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_TOMS_NAME;

@ApplicationScoped
@Named("UserRole")
@Slf4j
public class UserRole {
    @Inject
    private RoleRepository roleRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private UserPostRepository postRepository;

    public void setUserPost(AuthenticationFlowContext context) {
        final String DEBUG_STR = "setUserPost";
        log.info("{}: user={}", DEBUG_STR, context.getUser().getId());

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        final String tomsId = formData.getFirst("tomsId");
        final String selectedPostId = formData.get("postId").get(0);

        UserModel user = context.getUser();
        selectPostByUser(user, selectedPostId);
        user.setAttribute(ATTR_TOMS_NAME, Collections.singletonList(tomsId));
    }

    public void setUserPost(RequiredActionContext context) {
        final String DEBUG_STR = "setUserPost";
        log.info("{}: user={}", DEBUG_STR, context.getUser().getId());

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        final String tomsId = formData.getFirst("tomsId");
        final String selectedPostId = formData.get("postId").get(0);

        UserModel user = context.getUser();

        selectPostByUser(user, selectedPostId);
        user.setAttribute(ATTR_TOMS_NAME, Collections.singletonList(tomsId));
    }

    public void setUserPost(AuthenticationFlowContext context, List<UserPostResponse> attributes) {
        final String DEBUG_STR = "setUserPost";
        log.info("{}: user={}", DEBUG_STR, context.getUser().getId());

        final String tomsId = attributes.get(0).getTomsId();
        final String selectedPostId = attributes.get(0).getId();

        UserModel user = context.getUser();

        selectPostByUser(user, selectedPostId);
        user.setAttribute(ATTR_TOMS_NAME, Collections.singletonList(tomsId));
    }



    private List<UserPostEntity> deselectAllPostsByUser(UserEntity user) {
        List<UserPostEntity> userPosts = postRepository.getAllUserPostByUserId(user.getId());

        userPosts.forEach(post -> post.setSelected(false));

        unbindAllRolesToUser(user);
        return userPosts;
    }

    public void selectPostByUser(UserModel user, String selectedPostId) {
        UserEntity userEntity = userRepository.findUser(user.getId());
        UserPostEntity userPostEntities = deselectAllPostsByUser(userEntity).stream()
                .filter(p -> p.getId().equals(selectedPostId))
                .findFirst()
                .orElseThrow(ForbiddenException::new);
        userPostEntities.setSelected(true);
        bindRolesToUser(userEntity, userPostEntities);
    }

    private void unbindAllRolesToUser(UserEntity user) {
        Set<String> postRoleNames = new HashSet<>();

        postRoleNames.addAll(postRepository.getAllUserPostRoles().stream()
                .map(UserPostRoleEntity::getName).collect(Collectors.toSet()));

        postRoleNames.addAll(postRepository.getAllExternalSystemRoleForRealm(user.getRealmId()).stream()
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
        clientRole.setClientId(client.getClientId());
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
        roleEntity.setRealmId(realmEntity.getId());

        roleEntity.setRealmId(realmId);

        return roleEntity;
    }
}
