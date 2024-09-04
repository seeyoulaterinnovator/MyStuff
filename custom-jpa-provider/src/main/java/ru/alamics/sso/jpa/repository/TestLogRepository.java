package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import ru.alamics.sso.jpa.entity.TestLogEntity;

@ApplicationScoped
public class TestLogRepository {

    @Inject
    EntityManager em;

    @Transactional
    public TestLogEntity save(TestLogEntity testLogEntity) {
        if (testLogEntity.getId() == null) {
            testLogEntity.setId(getCountLog() + 1L);
        }
        em.persist(testLogEntity);
        em.flush();
        return testLogEntity;
    }

    public Integer getCountLog() {
        return em.createQuery("SELECT tl FROM TestLogEntity tl", TestLogEntity.class)
                .getResultList().size();
    }

}
