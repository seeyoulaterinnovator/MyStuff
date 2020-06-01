package ru.alamics.sso.jpa.repository;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.annotations.QueryHints;
import ru.alamics.sso.jpa.entity.status.CheckTableEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.persistence.*;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Stateless
@LocalBean
@Slf4j
public class StatusRepository {

    @PersistenceContext
    private EntityManager em;

    public void tryInsertNodeName(String nodeName) {

        try {
            log.info("Checking nodeName " + nodeName);
            try {
                Object name = em
                    .createNativeQuery("SELECT name FROM CHECK_TABLE WHERE name = :nodeName ")
                    .setParameter("nodeName", nodeName)
                    .getSingleResult();

            } catch (NoResultException ignored) {

                log.info("Inserting nodeName " + nodeName);
                em
                    .createNativeQuery("INSERT INTO CHECK_TABLE (name, updated) VALUES (:nodeName , CURRENT_TIMESTAMP());")
                    .setParameter("nodeName", nodeName)
                    .executeUpdate();
            }

        } catch (Exception e) {
            log.error("tryInsertNodeName", e);
        }
    }

    @Transactional
    @Lock(LockType.WRITE)
    public boolean checkStatusDb(String nodeName) {
        try {
            CheckTableEntity ent = em.find(CheckTableEntity.class, nodeName, LockModeType.OPTIMISTIC);

            ent.setUpdateTime(LocalDateTime.now());

            //em.unwrap(Session.class).update(ent);

            em.merge(ent);
            // em.flush();

            /*
            em
                .createNativeQuery("UPDATE CHECK_TABLE SET updated = CURRENT_TIMESTAMP() WHERE name = :nodeName")
                .setParameter("nodeName", nodeName)
                .setHint(QueryHints.NATIVE_LOCKMODE, LockModeType.NONE)
                .executeUpdate();
            */
        } catch (OptimisticLockException oe) {
            log.info("OptimisticLockException " + oe.getMessage());
        } catch (Exception e) {
            log.error("checkStatusDb", e);
            return false;
        }

        return true;
    }
}
