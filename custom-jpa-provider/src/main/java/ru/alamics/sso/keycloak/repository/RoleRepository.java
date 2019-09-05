package ru.alamics.sso.keycloak.repository;

import org.keycloak.models.jpa.entities.RoleEntity;
import org.keycloak.models.jpa.entities.UserRoleMappingEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;
import java.util.UUID;

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
}
