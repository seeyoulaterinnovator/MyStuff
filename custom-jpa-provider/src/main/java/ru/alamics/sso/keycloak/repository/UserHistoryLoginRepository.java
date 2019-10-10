package ru.alamics.sso.keycloak.repository;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.keycloak.entity.UserLoginHistory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Stateless
public class UserHistoryLoginRepository {

    @PersistenceContext
    private EntityManager em;

    public void findInactiveUsers (final long absenceTime, final String realmId) {
        log.info("findInactiveUsers: realmId={}, absenceTime={}", realmId, absenceTime);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime absence = now.minusSeconds(absenceTime);

        em.createNativeQuery(
                "insert into AUTO_LOCK_NOTIFICATION(id, user_id, sended_at, type, status)\n" +
                        "select uuid(), ue.ID, null, 'ABSENCE_NOTIFICATION', 'PREPARE'\n" +
                        "from USER_ENTITY ue\n" +
                        "         left join (select ul.*,\n" +
                        "                           max(ul.LOGINED_AT) over (PARTITION BY ul.USER_ID) date\n" +
                        "                    from USER_LOGIN_HISTORY ul\n" +
                        "                    group by ul.USER_ID) ulh on ue.ID = ulh.USER_ID\n" +
                        "         left join (select ab.*,\n" +
                        "                           max(ab.SENDED_AT) block\n" +
                        "                    from AUTO_LOCK_NOTIFICATION ab\n" +
                        "                    where ab.TYPE = 'ABSENCE_BLOCKING'\n" +
                        "                      and ab.STATUS = 'SENT'\n" +
                        "                    group by ab.USER_ID) ab on ue.ID = ab.USER_ID\n" +
                        "         left join (select aln.*,\n" +
                        "                           max(aln.SENDED_AT) notif\n" +
                        "                    from AUTO_LOCK_NOTIFICATION aln\n" +
                        "                    where aln.TYPE = 'ABSENCE_NOTIFICATION'\n" +
                        "                    group by aln.USER_ID) aln on ue.ID = aln.USER_ID\n" +
                        "where ue.ENABLED = true\n" +
                        "  and ue.REALM_ID = :realm_id\n" +
                        "  and (ulh.date <= :date or ulh.date is null)\n" +
                        "  and ((ab.block <= :date and ab.block >= aln.notif) or aln.notif is null)\n" +
                        "  and ue.SERVICE_ACCOUNT_CLIENT_LINK is null")
                .setParameter("date", absence)
                .setParameter("realm_id", realmId)
                .executeUpdate();
    }

    public UserLoginHistory save (UserLoginHistory history) {
        final String id = UUID.randomUUID().toString();
        history.setId(id);
        em.persist(history);
        em.flush();

        return history;
    }


}
