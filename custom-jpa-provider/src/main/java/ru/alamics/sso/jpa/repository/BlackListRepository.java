package ru.alamics.sso.jpa.repository;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.RequiredActionContext;
import ru.alamics.sso.jpa.entity.antifraud.BlackListEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
@LocalBean
public class BlackListRepository {
    @PersistenceContext
    private EntityManager em;

    public BlackListEntity findByIp(String ip) {
        return em.createQuery("select be from BlackListEntity be where be.ip = :ip", BlackListEntity.class)
                .setParameter("ip", ip)
                .getSingleResult();
    }

    public BlackListEntity findFirstByIp(String ip) {
        return em.createQuery("select be from BlackListEntity be where be.unblockedAt >= current_timestamp and be.ip =:ip", BlackListEntity.class)
                .setParameter("ip", ip)
                .getSingleResult();
    }

    public List<BlackListEntity> findByEmail(String email) {
        return em.createQuery("select be from BlackListEntity be where be.email = :email ORDER BY be.createdAt desc", BlackListEntity.class)
                .setParameter("email", email)
                .getResultList();
    }

    public BlackListEntity findFirstByLogin(String login) {
        return em.createQuery("select be from BlackListEntity be where be.unblockedAt >= current_timestamp and be.userLogin =:login", BlackListEntity.class)
                .setParameter("login", login)
                .getSingleResult();
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

    public void save(BlackListEntity entity) {
        em.persist(entity);
        em.flush();
    }

    public void update(BlackListEntity entity) {
        em.createNativeQuery("update BLACK_LIST set created=:now, unblocked=:unblocked, block_count=:count" +
                        " where phone =:phone and realm=:realm" +
                " and limitation_cause=:cause")
                .setParameter("phone", entity.getPhone())
                .setParameter("count", entity.getBlockCount())
                .setParameter("realm", entity.getRealm())
                .setParameter("cause", entity.getLimitationCause())
                .setParameter("unblocked", entity.getUnblockedAt())
                .setParameter("now", LocalDateTime.now())
                .executeUpdate();
    }

    public boolean isWasBlockedByPhoneRealmCause(String phone, String realm, String cause) {
        return !em.createQuery("select ble from BlackListEntity ble where ble.limitationCause=:cause and ble.realm=:realm and ble.phone=:phone", BlackListEntity.class)
                .setParameter("cause", cause)
                .setParameter("realm", realm)
                .setParameter("phone", phone)
                .getResultList().isEmpty();
    }
}
