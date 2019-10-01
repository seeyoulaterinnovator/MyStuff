package ru.alamics.sso.keycloak.repository;

import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.KeycloakModelUtils;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

    public UserEntity getUserByPhoneNumber(String phone) {
        var users =  em.createQuery("select u from UserEntity u join u.attributes attr \n" +
                "  where u.realmId = :realmId " +
                "       and attr.name = :name " +
                "       and attr.value = :phoneNmbr", UserEntity.class)
                .setParameter("realmId", "user")
                .setParameter("name", "phone")
                .setParameter("phoneNmbr", phone)
                .getResultList();
        if (users != null && users.size() > 0) {
            return users.get(0);
        }
        return null;
    }
}
