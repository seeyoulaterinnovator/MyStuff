package ru.alamics.sso.jpa.repository;

import ru.alamics.sso.jpa.entity.antifraud.AttemptFailsEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
@LocalBean
public class AttemptFailsRepository {
    @PersistenceContext
    private EntityManager em;

    public List<AttemptFailsEntity> getFailAttemptsByPhoneAndRealm(String phone, String realm, String cause) {
        return em.createQuery("select afe from AttemptFailsEntity afe where afe.phone =:phone and afe.realm =:realm" +
                        " and afe.limitationCause =:cause", AttemptFailsEntity.class)
                .setParameter("phone", phone)
                .setParameter("realm", realm)
                .setParameter("cause", cause)
                .getResultList();
    }

    public void save(AttemptFailsEntity entity) {
        em.persist(entity);
        em.flush();
    }

    public void delete(List<AttemptFailsEntity> entity) {
        if (entity.isEmpty()) {
            return;
        }
        entity.forEach(it -> em.createQuery("delete from AttemptFailsEntity afe where afe.phone=:phone and afe.realm =:realm and afe.limitationCause=:cause")
                .setParameter("phone", it.getPhone())
                .setParameter("realm", it.getRealm())
                .setParameter("cause", it.getLimitationCause())
                .executeUpdate());
    }
}
