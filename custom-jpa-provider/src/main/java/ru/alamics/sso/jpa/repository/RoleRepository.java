package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.keycloak.models.jpa.entities.ClientEntity;
import org.keycloak.models.jpa.entities.RoleEntity;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.jpa.entities.UserRoleMappingEntity;
import ru.alamics.sso.jpa.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleRepository {
    @Inject
    EntityManager em;

    public RoleEntity findById(String id) {
        return em.find(RoleEntity.class, id);
    }

    public RoleEntity findRoleEntityByName(final String roleName, final String realmId) {

        List<RoleEntity> resultList = em.createQuery("select re from RoleEntity re where re.name =:roleName and re.realmId =:realmId", RoleEntity.class)
                .setParameter("roleName", roleName)
                .setParameter("realmId", realmId)
                .getResultList();

        return CollectionUtils.nullOrGet(resultList, 0);

    }

    @Transactional
    public RoleEntity save(final RoleEntity entity) {
        entity.setId(UUID.randomUUID().toString());
        em.persist(entity);
        em.flush();
        return entity;
    }

    @Transactional
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

    @Transactional
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

        List<RoleEntity> resultList = em.createQuery("select re from RoleEntity re where re.name =:roleName and re.realmId =:realmId and re.clientId =:clientId", RoleEntity.class)
                .setParameter("roleName", roleName)
                .setParameter("realmId", realmId)
                .setParameter("clientId", clientEntity.getId())
                .getResultList();

        return CollectionUtils.nullOrGet(resultList, 0);

    }

    public ClientEntity findClientByName(final String name, final String realmId) {

        List<ClientEntity> resultList = em.createQuery("select c from ClientEntity c where c.clientId =:name and c.realmId =:realmId", ClientEntity.class)
                .setParameter("name", name)
                .setParameter("realmId", realmId)
                .getResultList();

        return CollectionUtils.nullOrGet(resultList, 0);

    }
}
