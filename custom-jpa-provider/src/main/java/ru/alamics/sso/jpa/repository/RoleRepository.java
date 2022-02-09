package ru.alamics.sso.jpa.repository;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.ClientEntity;
import org.keycloak.models.jpa.entities.RoleEntity;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.jpa.entities.UserRoleMappingEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Stateless
@LocalBean
public class RoleRepository {

    @PersistenceContext
    private EntityManager em;

    public RoleEntity findRoleEntityByName(final String roleName, final String realmId) {

        return em.createQuery("select re from RoleEntity re where re.name =:roleName and re.realm.id =:realmId", RoleEntity.class)
                .setParameter("roleName", roleName)
                .setParameter("realmId", realmId)
                .getResultList()
                .get(0);
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
        if (notExist) {
            em.persist(entity);
            em.flush();
        }
        return entity;
    }

    public List<RoleEntity> findRolesByNames(final List<String> names, final String realmId) {
        if (names.isEmpty()) {
            return Collections.emptyList();
        }

        return em.createQuery("select re from RoleEntity re where re.name in :roleNames and re.realmId =:realmId", RoleEntity.class)
                .setParameter("roleNames", names)
                .setParameter("realmId", realmId)
                .getResultList();
    }

    public void unbindRolesToUserByNames(final UserEntity user, final Set<String> roleNames) {

        List<String> roleIds = findRolesByNames(new ArrayList<>(roleNames), user.getRealmId()).stream()
                .map(RoleEntity::getId)
                .collect(Collectors.toList());

        if (!roleIds.isEmpty()) {
            em.createQuery("delete from UserRoleMappingEntity where user =:user and roleId in :roles")
                    .setParameter("user", user)
                    .setParameter("roles", roleIds)
                    .executeUpdate();
        }
    }

    public RoleEntity findClientRoleEntity(final String roleName, final String realmId, final ClientEntity clientEntity) {

        return em.createQuery("select re from RoleEntity re where re.name =:roleName and re.realmId =:realmId and re.client =:client", RoleEntity.class)
                .setParameter("roleName", roleName)
                .setParameter("realmId", realmId)
                .setParameter("client", clientEntity)
                .getResultList()
                .get(0);
    }

    public ClientEntity findClientByName(final String name, final String realmId) {

        return em.createQuery("select c from ClientEntity c where c.clientId =:name and c.realm.id =:realmId", ClientEntity.class)
                .setParameter("name", name)
                .setParameter("realmId", realmId)
                .getResultList()
                .get(0);
    }
}
