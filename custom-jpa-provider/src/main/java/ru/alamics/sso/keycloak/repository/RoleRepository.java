package ru.alamics.sso.keycloak.repository;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.*;
import ru.alamics.sso.keycloak.entity.ExternalSystemEntity;
import ru.alamics.sso.keycloak.entity.ExternalSystemRoleEntity;
import ru.alamics.sso.keycloak.entity.UserPostEntity;
import ru.alamics.sso.keycloak.entity.UserPostRoleEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Stateless
@LocalBean
public class RoleRepository {


    @PersistenceContext
    private EntityManager em;


    public RoleEntity findRoleEntity(final String roleName, final String realmId) {

        List<RoleEntity> ret = em.createQuery("select re from RoleEntity re where re.name =:roleName and re.realm.id =:realmId", RoleEntity.class)
                .setParameter("roleName", roleName)
                .setParameter("realmId", realmId)
                .getResultList();


        return ret.isEmpty() ? null : ret.get(0);
    }

    public RoleEntity save(final RoleEntity entity) {
        entity.setId(UUID.randomUUID().toString());
        em.persist(entity);
        em.flush();
        return entity;
    }

    public UserRoleMappingEntity save(final UserRoleMappingEntity entity) {
        boolean notExist = em.createQuery("select (count(ur) <= 0) as t from UserRoleMappingEntity ur where ur.roleId =:roleId and ur.user.id=:userId", Boolean.class)
                .setParameter("userId", entity.getUser().getId())
                .setParameter("roleId", entity.getRoleId())
                .getSingleResult();
        if(notExist) {
            em.persist(entity);
            em.flush();
        }
        return entity;
    }

    public List<RoleEntity> findRolesByNames(final List<String> names, final String realmId) {
        if(names.isEmpty()) {
            return Collections.emptyList();
        }

        List<RoleEntity> ret = em.createQuery("select re from RoleEntity re where re.name in :roleNames and re.realmId =:realmId", RoleEntity.class)
                .setParameter("roleNames", names)
                .setParameter("realmId", realmId)
                .getResultList();


        return ret;
    }

    public void deleteUserPostRoles(final UserEntity user, final Set<UserPostEntity> posts, final String realmId) {
        final String DEBUG_STR = "deleteUserPostRoles";
        log.debug("{}: user={}, realmId={}", DEBUG_STR, user.getId(), realmId);
        final List<String> names = posts.stream().map(UserPostEntity::getRole).map(UserPostRoleEntity::getName).collect(Collectors.toList());
        List<RoleEntity> roles = findRolesByNames(names, realmId);
        List<String> roleIds = roles.stream().map(RoleEntity::getId).collect(Collectors.toList());
        if(!roleIds.isEmpty()) {
            em.createQuery("delete from UserRoleMappingEntity where user =:user and roleId in :roles")
                    .setParameter("user", user)
                    .setParameter("roles", roleIds)
                    .executeUpdate();
        }
    }

    public void deleteUserSystemPostClientRoles (final UserEntity user, final Set<ExternalSystemRoleEntity> sustems, final String realmId) {
        final String DEBUG_STR = "deleteUserSystemPostRoles";
        log.debug("{}: user={}, realmId={}", DEBUG_STR, user.getId(), realmId);
        final List<String> names = sustems.stream().map(ExternalSystemRoleEntity::getName).collect(Collectors.toList());
        List<RoleEntity> roles = findRolesByNames(names, realmId);
        List<String> roleIds = roles.stream().map(RoleEntity::getId).collect(Collectors.toList());
        if(!roleIds.isEmpty()) {
            em.createQuery("delete from UserRoleMappingEntity where user =:user and roleId in :roles")
                    .setParameter("user", user)
                    .setParameter("roles", roleIds)
                    .executeUpdate();
        }
    }

    public RoleEntity findClientRoleEntity(final String roleName, final String realmId, final ClientEntity clientEntity) {

        List<RoleEntity> ret = em.createQuery("select re from RoleEntity re where re.name =:roleName and re.realmId =:realmId and re.client =:client", RoleEntity.class)
                .setParameter("roleName", roleName)
                .setParameter("realmId", realmId)
                .setParameter("client", clientEntity)
                .getResultList();


        return ret.isEmpty() ? null : ret.get(0);
    }

    public ClientEntity findClientByName(final String name, final String realmId) {
        List<ClientEntity> ret = em.createQuery("select c from ClientEntity c where c.clientId =:name and c.realm.id =:realmId", ClientEntity.class)
                .setParameter("name", name)
                .setParameter("realmId", realmId)
                .getResultList();
        return ret.isEmpty() ? null : ret.get(0);

    }
}
