package ru.alamics.sso.jpa.repository;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.auth_reg.AuthOrRegTypeEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@LocalBean
@Slf4j
public class AuthOrRegTypeRepository {

    @PersistenceContext
    private EntityManager em;

    public AuthOrRegTypeEntity findAuthOrRegType(int id) {
        return em.find(AuthOrRegTypeEntity.class, id);
    }

}
