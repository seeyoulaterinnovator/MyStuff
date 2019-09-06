package ru.alamics.sso.inactive;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.repository.UserHistoryLoginRepository;
import ru.alamics.sso.property.Property;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import java.util.List;

@Singleton
@Startup
@Slf4j
public class InactiveSchedule {

    @EJB
    private UserHistoryLoginRepository repository;

    @Context
    private KeycloakContext context;

    @Inject
    @Property("user.absence.days")
    private int absenceDays;

    @Schedule(minute = "*", second = "*/1", persistent = false)
    public void notificationInactiveUsers() {
        final String DEBUG_STR = "findInactiveUsers";
        log.info("start:{}", DEBUG_STR);
        List<UserEntity> users = repository.findInactiveUsers(absenceDays);

        log.info("stop:{}", DEBUG_STR);
    }
}
