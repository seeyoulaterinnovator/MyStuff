package ru.alamics.sso.keycloak.repository;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.UserLoginHistory;
import ru.alamics.sso.keycloak.entity.common.NotificationType;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Stateless
public class UserHistoryLoginRepository {

    @PersistenceContext
    private EntityManager em;

    public List<UserEntity> findInactiveUsers(final long absenceDays) {
        final String DEBUG_STR = "findInactiveUsers";
        log.debug("{}:", DEBUG_STR);
        LocalDate now = LocalDate.now();
        LocalDate absence = now.minusDays(absenceDays);

        List<UserEntity> ret = em.createQuery("select user from UserLoginHistory ul join ul.user user where ul.loginedAt <=:time and user.enabled = true " +
                "and not exists (select 1 from AutoLockNotification aln where aln.user = ul.user and aln.type =:notifType) " +
                "and ul.loginedAt = (select max(ul2.loginedAt) from UserLoginHistory ul2 where ul2.user = ul.user)", UserEntity.class)
                .setParameter("time", absence.atTime(LocalTime.MIN))
                .setParameter("notifType", NotificationType.ABSENCE_NOTIFICATION)
                .getResultList();

        return ret;
    }

    public UserLoginHistory save(UserLoginHistory history) {
        final String id = UUID.randomUUID().toString();
        history.setId(id);
        em.persist(history);
        em.flush();

        return history;
    }


}
