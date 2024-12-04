package ru.alamics.sso.db.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import ru.alamics.sso.jpa.entity.TestLogEntity;
import ru.alamics.sso.jpa.repository.TestLogRepository;

@ApplicationScoped
public class TestLogService {

    @Inject
    TestLogRepository testLogRepository;

    @ConfigProperty(name = "TEST_LOG_DB_ENABLED", defaultValue = "false")
    Boolean useTestLog;

    public void logIntoBd(String logInfo) {
        if (useTestLog) {
            testLogRepository.save(TestLogEntity.builder().log(logInfo).build());
        }
    }

}
