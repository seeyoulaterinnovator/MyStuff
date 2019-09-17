package ru.alamics.sso.keycloak.repository;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.UserLoginHistory;
import ru.alamics.sso.keycloak.entity.common.NotificationType;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Stateless
public class UserHistoryLoginRepository {

    @PersistenceContext
    private EntityManager em;

    public void findInactiveUsers (final long absenceDays) {
        final String DEBUG_STR = "findInactiveUsers";
        log.debug("{}:", DEBUG_STR);
        LocalDate now = LocalDate.now();
        LocalDate absence = now.minusDays(absenceDays);

        em.createNativeQuery("insert into auto_lock_notification(id, user_id, sended_at, type, status)\n" +
                "select uuid(), ll.USER_ID, null, 'ABSENCE_NOTIFICATION', 'PREPARE'\n" +
                "from (select ul.*, max(ul.LOGINED_AT) over (PARTITION BY ul.USER_ID) date\n" +
                "      from USER_LOGIN_HISTORY ul\n" +
                "      where NOT exists(select 1\n" +
                "                       from auto_lock_notification aln\n" +
                "                       where aln.USER_ID = ul.USER_ID\n" +
                "                         and aln.TYPE = 'ABSENCE_NOTIFICATION')\n" +
                "\n" +
                "      group by ul.USER_ID\n" +
                "      FOR UPDATE) ll\n" +
                "         join user_entity user on user.ID = ll.USER_ID\n" +
                "where ll.date <= :date\n" +
                "  and user.ENABLED = true")
                .setParameter("date", absence)
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
