package ru.alamics.sso.jpa.repository;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.antifraud.AttemptFailsEntity;
import ru.alamics.sso.jpa.entity.antifraud.WroteCodeAttemptsEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
@LocalBean
@Slf4j
public class WroteCodeAttemptsRepository {

    @PersistenceContext
    private EntityManager em;

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
