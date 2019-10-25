package ru.alamics.sso.keycloak.repository;

import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.validation.Validation;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@LocalBean
@Stateless
public class UserRepository {

    @PersistenceContext
    private EntityManager em;

    public UserEntity findUser(final String userId) {
        UserEntity ret = em.find(UserEntity.class, userId);

        return ret;
    }

    public List<UserEntity> save(List<UserEntity> entities) {
        entities.forEach(entity -> {
            if(entity.getId() == null) {
                entity.setId(KeycloakModelUtils.generateId());
                em.persist(entity);
            } else {
                em.merge(entity);
            }
        });
        em.flush();

        return entities;
    }

    public UserEntity getFirstUserByPhoneNumber(RealmModel realmModel, String phone, String excludedUserId) {

        if (Validation.isBlank(phone))
            return null;

        var users =  em.createQuery("select u from UserEntity u join u.attributes attr \n" +
                "  where u.realmId = :realmId " +
                "       and attr.name = :name " +
                "       and (:excludedUserId is null or u.id <> :excludedUserId) " +
                "       and attr.value = :phoneNmbr", UserEntity.class)
                .setParameter("realmId", realmModel == null ? "user" : realmModel.getId())
                .setParameter("name", "phone")
                .setParameter("phoneNmbr", phone)
                .setParameter("excludedUserId", Validation.isBlank(excludedUserId) ? null : excludedUserId)
                .getResultList();
        if (users != null && users.size() > 0) {
            return users.get(0);
        }
        return null;
    }

    public UserEntity getFirstUserByPhoneNumber(String phone, String excludedUserId) {

        if (Validation.isBlank(phone))
            return null;

        var users =  em.createQuery("select u from UserEntity u join u.attributes attr \n" +
                "  where attr.name = :name " +
                "       and (:excludedUserId is null or u.id <> :excludedUserId) " +
                "       and attr.value = :phoneNmbr", UserEntity.class)
                .setParameter("name", "phone")
                .setParameter("phoneNmbr", phone)
                .setParameter("excludedUserId", Validation.isBlank(excludedUserId) ? null : excludedUserId)
                .getResultList();
        if (users != null && users.size() > 0) {
            return users.get(0);
        }
        return null;
    }
}
