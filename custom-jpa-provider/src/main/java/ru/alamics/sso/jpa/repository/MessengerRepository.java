package ru.alamics.sso.jpa.repository;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.MessengerEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Slf4j
@Stateless
@LocalBean
public class MessengerRepository {

    @PersistenceContext
    private EntityManager em;

    public List<MessengerEntity> getAllMessenger() {
        return em.createQuery(
                "select mes " +
                        "from MessengerEntity mes", MessengerEntity.class)
                .getResultList();
    }

}
