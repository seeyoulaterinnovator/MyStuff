package ru.alamics.sso.inactive;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.repository.UserHistoryLoginRepository;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.List;

@Slf4j
@Singleton
@Startup
public class InactiveSchedule {

    @EJB
    private UserHistoryLoginRepository repository;

    @Schedule(hour = "*", second = "*/1", minute = "*")
    public void findInactiveUsers() {
        final String DEBUG_STR = "findInactiveUsers";
        log.info("start - {}", DEBUG_STR);

        List<UserEntity> users = repository.findInactiveUsers();
        log.info("stop - {}", DEBUG_STR);
    }
}
