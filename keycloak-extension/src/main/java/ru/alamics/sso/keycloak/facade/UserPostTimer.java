package ru.alamics.sso.keycloak.facade;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.property.ApplicationProperties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;

@Slf4j
@Startup
@Singleton
public class UserPostTimer {
    private static final String TIMER_NAME = "User Post Timer";
    private static final String TBAPI_REQUEST_INTERVAL_PROPERTY = "tbapi.customer.request.interval.milliseconds";
    private long TBAPI_REQUEST_INTERVAL = 10000;
    private long tbapiRequestInterval;

    @Resource
    private TimerService timerService;
    @EJB
    private UserPostFacade userPostFacade;
    @Resource(lookup = "java:global/domru-sso/ApplicationProperties")
    private ApplicationProperties properties;

    @PostConstruct
    private void init() {
        final TimerConfig timerConfig = new TimerConfig(TIMER_NAME, false);
        try {
            tbapiRequestInterval = Long.parseLong(properties.getProperty(TBAPI_REQUEST_INTERVAL_PROPERTY));
            timerService.createIntervalTimer(tbapiRequestInterval, tbapiRequestInterval, timerConfig);
            log.info("Timer:{} is created, interval duration set to value={} milliseconds ", TIMER_NAME, tbapiRequestInterval);
        } catch (Exception e) {
            timerService.createIntervalTimer(TBAPI_REQUEST_INTERVAL, TBAPI_REQUEST_INTERVAL, timerConfig);
            log.warn("Timer:{} is created; Error read configuration, interval duration set to default value={} milliseconds",
                    TIMER_NAME, TBAPI_REQUEST_INTERVAL);
        }
    }

    @Timeout
    public void schedule(Timer timer) {
        if (!TIMER_NAME.equals(timer.getInfo().toString())) {
            return;
        }

        log.debug("Started update customers by timer={}", timer.getInfo());
        userPostFacade.updateCustomers();
    }
}
