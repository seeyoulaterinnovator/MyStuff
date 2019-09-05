package ru.alamics.sso.keycloak.repository;

import org.keycloak.models.jpa.entities.UserEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@LocalBean
@Stateless
public class UserRepository {

    @PersistenceContext
    private EntityManager em;

    public UserEntity findUser(final String userId) {

        UserEntity ret = em.find(UserEntity.class, userId);

        return ret;
    }
}
