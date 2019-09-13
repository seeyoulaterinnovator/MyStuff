package ru.alamics.sso.keycloak.repository;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.AutoLockNotification;
import ru.alamics.sso.keycloak.entity.common.NotificationStatus;
import ru.alamics.sso.keycloak.entity.common.NotificationType;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Stateless
public class AutoLockNotificationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(List<AutoLockNotification> autoLockNotifications) {
        autoLockNotifications.forEach(autoLockNotification -> {
            autoLockNotification.setId(UUID.randomUUID().toString());
            entityManager.persist(autoLockNotification);
        });
        entityManager.flush();
    }

    public void findUsersToBlock(final int absenceDaysBlock) {
        final String DEBUG_STR = "findNonBlockingUsers";
        log.debug("{}: absenceDaysBlock={}", DEBUG_STR, absenceDaysBlock);
        LocalDate now = LocalDate.now();
        LocalDate absence = now.minusDays(absenceDaysBlock);
        entityManager.createNativeQuery("insert into auto_lock_notification(id, user_id, sended_at, type, status)\n" +
                "select uuid(), aln.USER_ID, null, 'ABSENCE_BLOCKING', 'PREPARE'\n" +
                "from auto_lock_notification aln\n" +
                "         join user_entity ue on aln.USER_ID = ue.ID\n" +
                "where ue.ENABLED = true\n" +
                "and aln.TYPE ='ABSENCE_NOTIFICATION' and aln.SENDED_AT <= :date\n" +
                "and not exists(select 1 from auto_lock_notification aln2 where aln2.TYPE = 'ABSENCE_BLOCKING' and aln.USER_ID = aln2.USER_ID)\n" +
                "for update")
                .setParameter("date", absence)
                .executeUpdate();
    }

    public List<AutoLockNotification> findNotifications() {
        final String DEBUG_STR = "findNotifications";

        List<AutoLockNotification> ret = entityManager.createQuery("select aln from AutoLockNotification aln where aln.status =:status", AutoLockNotification.class)
                .setParameter("status", NotificationStatus.PREPARE)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
        ret.forEach(lock -> {
            lock.setStatus(NotificationStatus.SENT);
            lock.setSendedAt(LocalDateTime.now());
            entityManager.merge(lock);
        });
        entityManager.flush();


        return ret;
    }
}
