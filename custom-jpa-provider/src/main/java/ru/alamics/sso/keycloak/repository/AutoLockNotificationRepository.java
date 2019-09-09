package ru.alamics.sso.keycloak.repository;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.AutoLockNotification;
import ru.alamics.sso.keycloak.entity.common.NotificationType;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
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

    public List<UserEntity> findNonBlockingUsers(final int absenceDaysBlock) {
        final String DEBUG_STR = "findNonBlockingUsers";
        log.debug("{}: absenceDaysBlock={}", DEBUG_STR, absenceDaysBlock);
        LocalDate now = LocalDate.now();
        LocalDate absence = now.minusDays(absenceDaysBlock);


        List<UserEntity> ret = entityManager.createQuery("select user from AutoLockNotification aln join aln.user user where user.enabled = true " +
                "and aln.type =:type and aln.sendedAt <= :time", UserEntity.class)
                .setParameter("type", NotificationType.ABSENCE_NOTIFICATION)
                .setParameter("time", absence.atTime(LocalTime.MIN))
                .getResultList();

        return ret;
    }
}
