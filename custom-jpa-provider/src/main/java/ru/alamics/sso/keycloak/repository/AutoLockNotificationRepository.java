package ru.alamics.sso.keycloak.repository;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.keycloak.entity.AutoLockNotification;
import ru.alamics.sso.keycloak.entity.common.NotificationStatus;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime absence = now.minusSeconds(absenceTimeBlock);
        int countUsersToBlock = entityManager.createNativeQuery(
                "insert into AUTO_LOCK_NOTIFICATION(id, user_id, sended_at, type, status)\n" +
                        "select uuid(), ue.ID, null, 'ABSENCE_BLOCKING', 'PREPARE'\n" +
                        "from USER_ENTITY ue\n" +
                        "         left join (select ab.*, max(ab.SENDED_AT) block\n" +
                        "                    from AUTO_LOCK_NOTIFICATION ab\n" +
                        "                    where ab.TYPE = 'ABSENCE_BLOCKING'\n" +
                        "                    group by ab.USER_ID) ab on ue.ID = ab.USER_ID\n" +
                        "         left join (select aln.*, max(aln.SENDED_AT) notif\n" +
                        "                    from AUTO_LOCK_NOTIFICATION aln\n" +
                        "                    where aln.TYPE = 'ABSENCE_NOTIFICATION'\n" +
                        "                      and aln.STATUS = 'SENT'\n" +
                        "                    group by aln.USER_ID) aln on ue.ID = aln.USER_ID\n" +
                        "where ue.ENABLED = true\n" +
                        "  and ue.REALM_ID = :realm_id\n" +
                        "  and ((aln.notif < :date and ab.block < aln.notif) or (ab.block is null and aln.notif < :date))")
                .setParameter("date", absence)
                .setParameter("realm_id", realmId)
                .executeUpdate();
        if (countUsersToBlock > 0) {
            log.info("findBlockingUsers: realmId={}, absenceTimeBlock={}, countUsersToBlock={}", realmId, absenceTimeBlock, countUsersToBlock);
        }
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
