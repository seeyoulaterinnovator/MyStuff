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

    public void findUsersToBlock(final long absenceTimeBlock, final String realmId) {
        log.info("findBlockingUsers: realmId={}, absenceTimeBlock={}", realmId, absenceTimeBlock);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime absence = now.minusSeconds(absenceTimeBlock);
        entityManager.createNativeQuery("" +
                "insert into AUTO_LOCK_NOTIFICATION(id, user_id, sended_at, type, status)\n" +
                "select uuid(), user_info.ID, null, 'ABSENCE_BLOCKING', 'PREPARE'\n" +
                "from (select ue.ID,\n" +
                "    ue.REALM_ID,\n" +
                "    ue.ENABLED,\n" +
                "    aln.max_date as notif,\n" +
                "    ab.max_date as block\n" +
                "    from USER_ENTITY ue\n" +
                "    left join (select ab.*, max(ab.SENDED_AT) max_date\n" +
                "    from AUTO_LOCK_NOTIFICATION ab where ab.TYPE = 'ABSENCE_BLOCKING' group by ab.USER_ID) ab on ue.ID = ab.USER_ID\n" +
                "    left join (select aln.*, max(aln.SENDED_AT) max_date\n" +
                "    from AUTO_LOCK_NOTIFICATION aln where aln.TYPE = 'ABSENCE_NOTIFICATION' and aln.STATUS = 'SENT' group by aln.USER_ID) aln on ue.ID = aln.USER_ID\n" +
                "    FOR UPDATE) user_info\n" +
                "where user_info.ENABLED = true\n" +
                "    and ((user_info.notif < :date and user_info.block < user_info.notif) or user_info.block is null)\n" +
                "    and user_info.REALM_ID = :realm_id")
                .setParameter("date", absence)
                .setParameter("realm_id", realmId)
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
