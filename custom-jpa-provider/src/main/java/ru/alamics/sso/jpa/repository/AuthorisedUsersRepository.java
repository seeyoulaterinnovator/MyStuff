package ru.alamics.sso.jpa.repository;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.auth_reg.AuthorisedUsersEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@LocalBean
@Slf4j
public class AuthorisedUsersRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(AuthorisedUsersEntity entity) {
        em.persist(entity);
        em.flush();
    }

}
