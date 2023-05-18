package ru.alamics.sso.jpa.repository;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.antifraud.AttemptFailsEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
@LocalBean
@Slf4j
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

    public List<AttemptFailsEntity> getFailAttemptsIfWasBlocked(String phone, String realm, String cause) {
        Query query = em.createNativeQuery("WITH phones AS (" +
                "  SELECT phone, realm, unblocked " +
                "  FROM BLACK_LIST " +
                "  WHERE phone =:phone " +
                "    AND realm =:realm " +
                "    AND limitation_cause =:cause " +
                ") " +
                "SELECT af.* " +
                "FROM ATTEMPT_FAILS af " +
                "INNER JOIN phones ON phones.phone = af.phone and phones.realm = af.realm where af.created >= phones.unblocked", AttemptFailsEntity.class)
                .setParameter("phone", phone)
                .setParameter("realm", realm)
                .setParameter("cause", cause);

        return (List<AttemptFailsEntity>) query.getResultList();
    }

    public List<AttemptFailsEntity> getFailAttemptsIfAuthSuccess(String phone, String realm, String cause) {
        Query query = em.createNativeQuery("select * " +
                        "from ATTEMPT_FAILS af " +
                        "where created >= (select ul.LOGINED_AT " +
                        "                  from USER_LOGIN_HISTORY ul " +
                        "                  where phone =:phone " +
                        "                      and ul.is_success = true " +
                        "                  order by created desc " +
                        "                  limit 1) " +
                        "and af.phone =:phone and af.realm =:realm;", AttemptFailsEntity.class)
                .setParameter("phone", phone)
                .setParameter("realm", realm)
                .setParameter("cause", cause);

        return (List<AttemptFailsEntity>) query.getResultList();
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
