package ru.alamics.sso.inactive;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.keycloak.repository.AutoLockNotificationRepository;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.property.PropertyConstants;


import javax.annotation.PostConstruct;
import javax.ejb.*;


@Slf4j
@Singleton
@Startup
@DependsOn("ApplicationProperties")
public class InactiveBlockSchedule {
    @EJB
    private AutoLockNotificationRepository autoLockNotificationRepository;

    @EJB
    private ApplicationProperties properties;

    @Schedule(hour = "*", minute = "*/5", persistent = false)
    public void block () {
        final String DEBUG_STR = "block";
        log.info("start:{}", DEBUG_STR);
        Long absenceTimeBlock = Long.parseLong(properties.getProperty(PropertyConstants.ABSENCE_BLOCKING_DAYS, "user", true));
        if(absenceTimeBlock > -1) {
            autoLockNotificationRepository.findUsersToBlock(absenceTimeBlock);
        }
        log.info("stop:{}", DEBUG_STR);
    }
}
