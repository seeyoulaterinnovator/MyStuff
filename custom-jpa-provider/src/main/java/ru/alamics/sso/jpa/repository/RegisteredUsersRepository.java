package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import ru.alamics.sso.jpa.entity.auth_reg.RegisteredUsersEntity;

@ApplicationScoped
public class RegisteredUsersRepository {
    @Inject
    private EntityManager em;

    public void save(RegisteredUsersEntity entity) {
        em.persist(entity);
        em.flush();
    }
}
