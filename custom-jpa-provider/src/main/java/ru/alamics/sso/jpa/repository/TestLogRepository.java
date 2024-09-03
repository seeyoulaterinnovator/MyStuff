package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.TestLogEntity;

@ApplicationScoped
@Slf4j
public class TestLogRepository {

    @Inject
    EntityManager em;

    private static long megaCostul = 0L;

    @Transactional
    public TestLogEntity save(TestLogEntity testLogEntity) {
        if (testLogEntity.getId() == null) {
            megaCostul += 1L;
            testLogEntity.setId(megaCostul);
        }
        em.persist(testLogEntity);
        em.flush();
        return testLogEntity;
    }

}
