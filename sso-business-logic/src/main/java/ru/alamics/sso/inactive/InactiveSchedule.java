package ru.alamics.sso.inactive;

import lombok.extern.slf4j.Slf4j;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Slf4j
@Singleton
@Startup
public class InactiveSchedule {

    @Schedule(hour = "*", second = "*/1", minute = "*")
    public void findInactiveUsers() {
        final String DEBUG_STR = "findInactiveUsers";
        log.info("start - {}", DEBUG_STR);


        log.info("stop - {}", DEBUG_STR);
    }

}
