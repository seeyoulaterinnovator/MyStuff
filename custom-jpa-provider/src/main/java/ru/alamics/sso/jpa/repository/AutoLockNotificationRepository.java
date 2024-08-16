package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.AutoLockNotification;
import ru.alamics.sso.jpa.entity.common.NotificationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class AutoLockNotificationRepository {
    @Inject
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

        ret.forEach(lock -> {
            lock.setStatus(NotificationStatus.SENT);
            lock.setSendedAt(LocalDateTime.now());
            entityManager.merge(lock);
        });
        log.info("{}: update notifications = SENT (count = {})", DEBUG_STR, ret.size());
        entityManager.flush();

        return ret;
    }
}
