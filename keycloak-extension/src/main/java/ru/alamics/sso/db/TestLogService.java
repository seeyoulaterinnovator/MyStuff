package ru.alamics.sso.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.TestLogEntity;
import ru.alamics.sso.jpa.repository.TestLogRepository;

@ApplicationScoped
@Slf4j
public class TestLogService {

    @Inject
    TestLogRepository testLogRepository;

    public void logIntoBd(String logInfo) {
        testLogRepository.save(TestLogEntity.builder().log(logInfo).build());
    }

}
