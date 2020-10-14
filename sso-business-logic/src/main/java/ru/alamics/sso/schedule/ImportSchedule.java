package ru.alamics.sso.schedule;


import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.ImportUsersReportEntity;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.jpa.repository.*;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.user.ImportReportService;
import ru.alamics.sso.user.ImportService;
import ru.alamics.sso.user.model.ImportUsersReportModel;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.ejb.Timer;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Startup
@Singleton
@DependsOn("ApplicationProperties")
public class ImportSchedule {
    private static final String TIMER_NAME = "Import Schedule Timer";
    private static final long DEFAULT_INTERVAL_DURATION = 60000;
    private final static String TIMER_INTERVAL_DURATION_PROPERTY = "application.schedule.import.milliseconds";

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

    @PostConstruct
    private void init() {
        //final TimerConfig timerConfig = new TimerConfig(TIMER_NAME, false);

        final long intervalDuration = properties.getPropertyLong(TIMER_INTERVAL_DURATION_PROPERTY, DEFAULT_INTERVAL_DURATION);
        //timerService.createIntervalTimer(DEFAULT_INTERVAL_DURATION, intervalDuration, timerConfig);
        //log.info("Timer:{} is created, interval duration set to value={} milliseconds ", TIMER_NAME, intervalDuration);

        this.scheduler.scheduleAtFixedRate(this::schedule,
                DEFAULT_INTERVAL_DURATION, intervalDuration,
                TimeUnit.MILLISECONDS);
    }

    //@Timeout
    public void schedule(/*Timer timer*/) {
        //if (timer != null && !TIMER_NAME.equals(timer.getInfo().toString())) {
        //    return;
        //}

        List<ImportUsersReportModel> reportList = importReportService.getReportListByStatus(ImportUsersReportStatus.AWAITING);

        for (ImportUsersReportModel en : reportList) {
            importReportService.setReportStatus(en, ImportUsersReportStatus.IN_PROGRESS);
        }

        for (ImportUsersReportModel reportModel : reportList) {
            importService.createImportUsers(reportModel, null);
        }
    }

}
