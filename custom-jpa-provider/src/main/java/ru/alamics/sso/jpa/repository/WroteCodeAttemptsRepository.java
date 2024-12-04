package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import ru.alamics.sso.jpa.entity.antifraud.WroteCodeAttemptsEntity;

import java.util.List;

@ApplicationScoped
public class WroteCodeAttemptsRepository {
    @Inject
    EntityManager em;

    @Transactional
    public void save(WroteCodeAttemptsEntity entity) {
        em.persist(entity);
        em.flush();
    }

    public List<WroteCodeAttemptsEntity> getWroteCodeAttemptsByCode(String phone, String realm, String type, String code){
        return em.createQuery("select afe from WroteCodeAttemptsEntity afe where afe.phone =:phone and afe.realm =:realm" +
                        " and afe.typeSend =:cause and afe.code =:code", WroteCodeAttemptsEntity.class)
                .setParameter("phone", phone)
                .setParameter("realm", realm)
                .setParameter("cause", type)
                .setParameter("code", code)
                .getResultList();
    }


}
