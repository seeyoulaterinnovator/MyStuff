package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import ru.alamics.sso.jpa.entity.status.CheckTableEntity;

import java.time.LocalDateTime;

@ApplicationScoped
@Slf4j
public class StatusRepository {
    @Inject
    EntityManager em;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void tryInsertNodeName(String nodeName) {

        try {
            log.info("Checking nodeName " + nodeName);
            try {
                em
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

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public boolean checkStatusDb(String nodeName) {
        try {
            CheckTableEntity ent = em.find(CheckTableEntity.class, nodeName);

            ent.setUpdateTime(LocalDateTime.now());

            em.unwrap(Session.class).merge(ent);

        } catch (OptimisticLockException oe) {
            log.info("OptimisticLockException " + oe.getMessage());
        } catch (Exception e) {
            log.error("checkStatusDb", e);
            return false;
        }

        return true;
    }
}
