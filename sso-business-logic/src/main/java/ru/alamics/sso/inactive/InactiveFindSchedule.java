package ru.alamics.sso.inactive;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.keycloak.repository.UserHistoryLoginRepository;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.property.PropertyConstants;

import javax.annotation.PostConstruct;
import javax.ejb.*;


@Slf4j
@Singleton
@Startup
@DependsOn("ApplicationProperties")
public class InactiveFindSchedule {

    @EJB
    private UserHistoryLoginRepository userHistoryLoginRepository;
    @EJB
    private ApplicationProperties properties;


    //TODO довести до ума inject пропертей
//    @Inject
//    @Property(value = "user.absence.notifications.days")
    private Long absenceTimeNotification;

    @Schedule(hour = "*/3", persistent = false)
    public void notificationInactiveUsers () {
        final String DEBUG_STR = "findNotifications";
        log.info("start:{}", DEBUG_STR);
        if(absenceTimeNotification > -1) {
            userHistoryLoginRepository.findInactiveUsers(absenceTimeNotification);
        }
        log.info("stop:{}", DEBUG_STR);
    }


    @PostConstruct
    public void init() {
        this.absenceTimeNotification = Long.parseLong(properties.getProperty(PropertyConstants.ABSENCE_NOTIFICATION_DAYS, "user"));
    }
}
