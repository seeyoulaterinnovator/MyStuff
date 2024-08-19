package ru.alamics.sso.schedule;


import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.scheduled.ClusterAwareScheduledTaskRunner;
import org.keycloak.timer.ScheduledTask;
import org.keycloak.timer.TimerProvider;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.user.ImportReportService;
import ru.alamics.sso.user.ImportService;
import ru.alamics.sso.user.model.ImportUsersReportModel;
import ru.alamics.sso.user.model.RepeatNextTimeException;

import java.util.List;

@ApplicationScoped
@Slf4j
public class ImportSchedule implements ScheduledTask {
    private static final String TIMER_NAME = "Import Schedule Timer";
    private static final long DEFAULT_INTERVAL_DURATION = 60000;
    private final static String TIMER_INTERVAL_DURATION_PROPERTY = "application.schedule.import.milliseconds";

    private static final long MAX_TIMEOUT_MILLI = (4 * 60 + 45) * 1000L;

    @Inject
    ImportReportService importReportService;
    @Inject
    ApplicationProperties properties;
    @Inject
    ImportService importService;

    @Context
    KeycloakSession session;

    TimerProvider timerProvider;

    public static void checkTimeout(Long scheduleStart) throws RepeatNextTimeException {

        if (scheduleStart == null)
            return;

        long now = System.currentTimeMillis();
        if (now - scheduleStart > MAX_TIMEOUT_MILLI)
            throw new RepeatNextTimeException();
    }

    @PostConstruct
    private void init() {
        timerProvider = session.getProvider(TimerProvider.class);
    }

    void onStart(@Observes StartupEvent ev) {
        long intervalDuration = properties.getPropertyLong(TIMER_INTERVAL_DURATION_PROPERTY, DEFAULT_INTERVAL_DURATION);
        if (intervalDuration <= 0) {
            intervalDuration = DEFAULT_INTERVAL_DURATION;
        }

        timerProvider.cancelTask(TIMER_NAME);
        timerProvider.schedule(
                new ClusterAwareScheduledTaskRunner(session.getKeycloakSessionFactory(), this, intervalDuration),
                intervalDuration
        );
        log.info("Timer:{} is created, interval duration set to value={} milliseconds ", TIMER_NAME, intervalDuration);
    }

    @Override
    public String getTaskName() {
        return TIMER_NAME;
    }

    @Override
    public void run(KeycloakSession session) {
        long scheduleStart = System.currentTimeMillis();

        List<ImportUsersReportModel> reportList = importReportService.getReportListByStatus(ImportUsersReportStatus.AWAITING);

        for (ImportUsersReportModel reportModel : reportList) {
            importReportService.setReportStatus(reportModel, ImportUsersReportStatus.IN_PROGRESS);
            importService.createImportUsers(reportModel, null, scheduleStart, null, null);
            importReportService.updateReport(reportModel);
        }
    }
}
