package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.MessengerEntity;

import java.util.List;

@ApplicationScoped
@Slf4j
public class MessengerRepository {
    @Inject
    private EntityManager em;

    public List<MessengerEntity> getAllMessenger() {
        return em.createQuery("select mes from MessengerEntity mes", MessengerEntity.class)
                .getResultList();
    }

}
