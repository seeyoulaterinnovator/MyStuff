package ru.alamics.sso.jpa.repository;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.AutoLockNotification;
import ru.alamics.sso.jpa.entity.common.NotificationStatus;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    public List<AutoLockNotification> findNotifications() {
        final String DEBUG_STR = "findNotifications";

        List<AutoLockNotification> ret = entityManager.createQuery("select aln from AutoLockNotification aln where aln.status =:status", AutoLockNotification.class)
                .setParameter("status", NotificationStatus.PREPARE)
//                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .setMaxResults(100)
                .getResultList();

        // TODO set SENT only after successful send
        ret.forEach(lock -> {
            lock.setStatus(NotificationStatus.SENT);
            lock.setSendedAt(LocalDateTime.now());
            entityManager.merge(lock);
        });
        log.info("{}: update notifications = SENT ", DEBUG_STR);
        entityManager.flush();

        return ret;
    }
}
