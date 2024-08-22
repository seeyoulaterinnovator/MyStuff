package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import ru.alamics.sso.jpa.entity.auth_reg.RegisteredUsersEntity;

@ApplicationScoped
public class RegisteredUsersRepository {
    @Inject
    EntityManager em;

    @Transactional
    public void save(RegisteredUsersEntity entity) {
        em.persist(entity);
        em.flush();
    }
}
