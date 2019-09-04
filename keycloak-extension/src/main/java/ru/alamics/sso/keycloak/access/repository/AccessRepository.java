package ru.alamics.sso.keycloak.access.repository;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.keycloak.access.entity.Access;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.UUID;
import javax.ejb.Stateless;

@Slf4j
@Stateless
public class AccessRepository {

    @PersistenceContext
    private EntityManager em;

    public Access save(Access access) {
        em.persist(access);
        em.flush();
        return access;
    }

    public Access update(Access access) {
        if (access.getId().isBlank()){
            access.setId(UUID.randomUUID().toString());
        }
        em.merge(access);
        em.flush();
        return access;
    }

    public void remove(Access access) {
        em.remove(access);
        em.flush();
    }

    public Access getAccess(String id) {
        Access access = em.find(Access.class, id);
        em.flush();
        return access;
    }

    public Access getAccess(String userId, String tomsId) {
        Access access = em.createQuery(
                "select ac " +
                        "from Access ac " +
                        "where ac.toms_id = :toms_id and ac.user_id = :user_id", Access.class)
                .setParameter("user_id", userId)
                .setParameter("toms_id", tomsId)
                .getSingleResult();
        em.flush();
        return access;
    }
}
