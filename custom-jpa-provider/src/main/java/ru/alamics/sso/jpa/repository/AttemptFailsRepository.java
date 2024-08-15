package ru.alamics.sso.jpa.repository;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.jpa.entity.antifraud.AttemptFailsEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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

    public List<AttemptFailsEntity> getFailAttemptsIfAuthSuccess(String phone, String realm, String cause, UserEntity user) {
        Query query = em.createNativeQuery("with login_tries as (select * " +
                        "                     from USER_LOGIN_HISTORY ul " +
                        "                     where ul.USER_ID = :userId " +
                        "                       and ul.is_success = true " +
                        "                     order by ul.LOGINED_AT desc " +
                        "                     limit 1) " +
                        " " +
                        "select af.* " +
                        "from ATTEMPT_FAILS af " +
                        "         inner join login_tries lt on af.user_id = lt.USER_ID " +
                        "where af.created >= lt.LOGINED_AT " +
                        "  and af.phone = :phone " +
                        "  and af.realm = :realm " +
                        "  and af.limitation_cause = :cause", AttemptFailsEntity.class)
                .setParameter("phone", phone)
                .setParameter("realm", realm)
                .setParameter("cause", cause)
                .setParameter("userId", user.getId());

        return (List<AttemptFailsEntity>) query.getResultList();
    }

    public int getActualAttemptFailsCount(String phone, String realm, String cause, String userId) {
        return ((Number) em.createNativeQuery(
                "select count(*) from ATTEMPT_FAILS\n" +
                        "where limitation_cause = :cause and phone = :phone\n" +
                        "    and user_id = :userId and realm = :realm\n" +
                        "    and (\n" +
                        "        not exists(select *\n" +
                        "               from USER_LOGIN_HISTORY\n" +
                        "               where user_id = :userId and realm = :realm and is_success = 1)\n" +
                        "        or created > (select max(LOGINED_AT)\n" +
                        "                         from USER_LOGIN_HISTORY\n" +
                        "                         where user_id = :userId and realm = :realm and is_success = 1)\n" +
                        "    ) and (\n" +
                        "        not exists(select * from BLACK_LIST\n" +
                        "                   where limitation_cause = :cause and user_id = :userId and realm = :realm)\n" +
                        "        or created > (select max(unblocked) from BLACK_LIST\n" +
                        "                        where limitation_cause = :cause and user_id = :userId and realm = :realm)\n" +
                        "    );"
                )
                .setParameter("phone", phone)
                .setParameter("realm", realm)
                .setParameter("cause", cause)
                .setParameter("userId", userId)
                .getSingleResult()).intValue();
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
