package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import ru.alamics.sso.jpa.entity.auth_reg.AuthorisedUsersEntity;

@ApplicationScoped
public class AuthorisedUsersRepository {
    @Inject
    EntityManager em;

    @Transactional
    public void save(AuthorisedUsersEntity entity) {
        em.persist(entity);
        em.flush();
    }

}
