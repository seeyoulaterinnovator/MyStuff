package ru.alamics.sso.inactive;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.repository.impl.UserHistoryLoginRepositoryImpl;
import ru.alamics.sso.property.Property;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import java.util.List;


@Startup
@Slf4j
@Singleton
public class InactiveSchedule {

    @Inject
    private UserHistoryLoginRepositoryImpl repository;

    @Context
    private KeycloakSession session;

    @Inject
    @Property("user.absence.days")
    private int absenceDays;

//    @Schedule(hour = "*", second = "*/1", minute = "*", persistent = false)
    public void notificationInactiveUsers() {
        final String DEBUG_STR = "findInactiveUsers";log.info("start:{}", DEBUG_STR);
        List<UserEntity> users = repository.findInactiveUsers(absenceDays);

        log.info("stop:{}", DEBUG_STR);
    }
}
