package ru.alamics.sso.jpa.repository;

import org.hibernate.Session;
import ru.alamics.sso.jpa.entity.antifraud.BlackListEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

    public List<BlackListEntity> findBlockedByPhone(String phone) {
        return em.createQuery("select be from BlackListEntity be where be.phone =:phone and be.unblockedAt >= current_timestamp ORDER BY be.createdAt desc", BlackListEntity.class)
                .setParameter("phone", phone)
                .getResultList();

    }

    public void save(BlackListEntity entity) {
        em.persist(entity);
        em.flush();
    }
}
