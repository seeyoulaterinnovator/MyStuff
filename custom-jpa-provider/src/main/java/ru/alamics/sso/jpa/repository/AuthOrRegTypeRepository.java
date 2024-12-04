package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import ru.alamics.sso.jpa.entity.auth_reg.AuthOrRegTypeEntity;

@ApplicationScoped
public class AuthOrRegTypeRepository {
    @Inject
    EntityManager em;

    public AuthOrRegTypeEntity findAuthOrRegType(int id) {
        return em.find(AuthOrRegTypeEntity.class, id);
    }

}
