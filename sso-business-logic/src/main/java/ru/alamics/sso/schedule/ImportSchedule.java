package ru.alamics.sso.schedule;


import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.user.ImportReportService;
import ru.alamics.sso.user.ImportService;
import ru.alamics.sso.user.model.ImportUsersReportModel;
import ru.alamics.sso.user.model.RepeatNextTimeException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Startup
@Singleton
@DependsOn("ApplicationProperties")
public class ImportSchedule {
    private static final String TIMER_NAME = "Import Schedule Timer";
    private static final long DEFAULT_INTERVAL_DURATION = 60000;
    private final static String TIMER_INTERVAL_DURATION_PROPERTY = "application.schedule.import.milliseconds";

    private static final long MAX_TIMEOUT_MILLI = (4 * 60 + 45) * 1000L;

    @EJB
    private ImportReportService importReportService;
    @EJB
    private ApplicationProperties properties;
    @EJB
    private ImportService importService;
    //@Resource
    //private TimerService timerService;

    @Resource
    private ManagedScheduledExecutorService scheduler;

    public static void checkTimeout(Long scheduleStart) throws RepeatNextTimeException {

        if (scheduleStart == null)
            return;

        long now = System.currentTimeMillis();
        if (now - scheduleStart > MAX_TIMEOUT_MILLI)
            throw new RepeatNextTimeException();
    }

    @PostConstruct
    private void init() {
        //final TimerConfig timerConfig = new TimerConfig(TIMER_NAME, false);

        final long intervalDuration = properties.getPropertyLong(TIMER_INTERVAL_DURATION_PROPERTY, DEFAULT_INTERVAL_DURATION);
        //timerService.createIntervalTimer(DEFAULT_INTERVAL_DURATION, intervalDuration, timerConfig);
        //log.info("Timer:{} is created, interval duration set to value={} milliseconds ", TIMER_NAME, intervalDuration);

        this.scheduler.scheduleWithFixedDelay(this::schedule,
                DEFAULT_INTERVAL_DURATION, intervalDuration,
                TimeUnit.MILLISECONDS);
    }

    //@Timeout
    public void schedule(/*Timer timer*/) {
        //if (timer != null && !TIMER_NAME.equals(timer.getInfo().toString())) {
        //    return;
        //}
        try {

            long scheduleStart = System.currentTimeMillis();

            List<ImportUsersReportModel> reportList = importReportService.getReportListByStatus(ImportUsersReportStatus.AWAITING);

            for (ImportUsersReportModel reportModel : reportList) {
                importReportService.setReportStatus(reportModel, ImportUsersReportStatus.IN_PROGRESS);
                importService.createImportUsers(reportModel, null, scheduleStart, null, null);
                importReportService.updateReport(reportModel);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
