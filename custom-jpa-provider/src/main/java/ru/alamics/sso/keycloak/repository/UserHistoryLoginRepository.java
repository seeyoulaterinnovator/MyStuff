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
        log.info("findInactiveUsers: time={}, realmId={}", absenceTime, realmId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime absence = now.minusSeconds(absenceTime);

        em.createNativeQuery("" +
                "insert into AUTO_LOCK_NOTIFICATION(id, user_id, sended_at, type, status)\n" +
                "select uuid(), user_info.ID, null, 'ABSENCE_NOTIFICATION', 'PREPARE'\n" +
                "from (select ue.ID,\n" +
                "             ue.REALM_ID,\n" +
                "             ue.ENABLED,\n" +
                "             ulh.date,\n" +
                "             aln.max_date as notif,\n" +
                "             ab.max_date as block\n" +
                "      from USER_ENTITY ue\n" +
                "      left join\n" +
                "           (select ul.*, max(ul.LOGINED_AT) over (PARTITION BY ul.USER_ID) date\n" +
                "            from USER_LOGIN_HISTORY ul\n" +
                "            group by ul.USER_ID) ulh on ue.ID = ulh.USER_ID\n" +
                "      left join (select ab.*, max(ab.SENDED_AT) max_date\n" +
                "                 from AUTO_LOCK_NOTIFICATION ab where ab.TYPE = 'ABSENCE_BLOCKING' and ab.STATUS = 'SENT' group by ab.USER_ID) ab on ue.ID = ab.USER_ID\n" +
                "      left join (select aln.*, max(aln.SENDED_AT) max_date\n" +
                "                 from AUTO_LOCK_NOTIFICATION aln where aln.TYPE = 'ABSENCE_NOTIFICATION' group by aln.USER_ID) aln on ue.ID = aln.USER_ID\n" +
                "      FOR UPDATE) user_info\n" +
                "where (user_info.date < :date or user_info.date is null)\n" +
                "  and user_info.ENABLED = true\n" +
                "  and ((user_info.block < :date and user_info.block > user_info.notif) or user_info.notif is null)\n" +
                "  and user_info.REALM_ID = :realm_id")
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
