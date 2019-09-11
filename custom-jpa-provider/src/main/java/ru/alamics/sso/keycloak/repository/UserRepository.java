package ru.alamics.sso.keycloak.repository;

import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.KeycloakModelUtils;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

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
}
