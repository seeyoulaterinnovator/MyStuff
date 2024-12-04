package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.RequiredActionContext;
import ru.alamics.sso.jpa.entity.antifraud.BlackListEntity;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class BlackListRepository {
    @Inject
    EntityManager em;

    public List<BlackListEntity> findByEmail(String email) {
        return em.createQuery("select be from BlackListEntity be where be.email = :email ORDER BY be.createdAt desc", BlackListEntity.class)
                .setParameter("email", email)
                .getResultList();
    }

    public List<BlackListEntity> findFirstByPhoneAndLimitationCause(String phone, String limitationCause) {
        return em.createQuery("select be from BlackListEntity be " +
                        "where be.phone = :phone and be.limitationCause = :limitationCause " +
                        "order by be.createdAt desc", BlackListEntity.class)
                .setParameter("phone", phone)
                .setParameter("limitationCause", limitationCause)
                .getResultList();
    }

    public List<BlackListEntity> findBlockedByPhone(String phone, AuthenticationFlowContext context) {
        return em.createQuery("select be from BlackListEntity be where be.phone =:phone and be.unblockedAt >= :now and be.realm =:realm", BlackListEntity.class)
                .setParameter("phone", phone)
                .setParameter("now", LocalDateTime.now()) //current_timestamp doesn't work
                .setParameter("realm", context.getRealm().getName())
                .getResultList();
    }
    public List<BlackListEntity> findBlockedByPhone(String phone, RequiredActionContext context) {
        return em.createQuery("select be from BlackListEntity be where be.phone =:phone and be.unblockedAt >= :now and be.realm =:realm", BlackListEntity.class)
                .setParameter("phone", phone)
                .setParameter("now", LocalDateTime.now()) //current_timestamp doesn't work
                .setParameter("realm", context.getRealm().getName())
                .getResultList();
    }

    @Transactional
    public void save(BlackListEntity entity) {
        em.persist(entity);
        em.flush();
    }

    @Transactional
    public void update(BlackListEntity entity) {
        em.createNativeQuery("update BLACK_LIST set created=:now, unblocked=:unblocked, block_count=:count, block_duration=:block_dur" +
                        " where phone =:phone and realm=:realm" +
                " and limitation_cause=:cause")
                .setParameter("phone", entity.getPhone())
                .setParameter("count", entity.getBlockCount())
                .setParameter("realm", entity.getRealm())
                .setParameter("cause", entity.getLimitationCause())
                .setParameter("unblocked", entity.getUnblockedAt())
                .setParameter("now", LocalDateTime.now())
                .setParameter("block_dur", entity.getBlockDurationSec())
                .executeUpdate();
    }

    public boolean isWasBlockedByPhoneRealmCause(String phone, String realm, String cause) {
        return !em.createQuery("select ble from BlackListEntity ble where ble.limitationCause=:cause and ble.realm=:realm and ble.phone=:phone", BlackListEntity.class)
                .setParameter("cause", cause)
                .setParameter("realm", realm)
                .setParameter("phone", phone)
                .getResultList().isEmpty();
    }

    public List<BlackListEntity> findBlockedByPhoneRealmCause(String phone, String realm, String cause) {
        return em.createQuery("select ble from BlackListEntity ble where ble.limitationCause=:cause and ble.realm=:realm and ble.phone=:phone", BlackListEntity.class)
                .setParameter("cause", cause)
                .setParameter("realm", realm)
                .setParameter("phone", phone)
                .getResultList();
    }
}
