package ru.alamics.sso.credential;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.PasswordPolicy;
import ru.alamics.sso.keycloak.repository.PolicyRepository;
import ru.alamics.sso.property.ApplicationProperties;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.concurrent.TimeUnit;

@Startup
@Singleton
@Slf4j
public class PasswordExpiresSchedule {

    @EJB
    private PolicyRepository policyRepository;

    @EJB
    private ApplicationProperties properties;

    private String host;

    @Schedule(hour = "*", second = "*/10", minute = "*", persistent = false)
    public void findExpiredPassword() {
        final String DEBUG_STR = "findExpiredPassword";
        log.info("start: {}", DEBUG_STR);
        var realms = policyRepository.findRealmWithPolicy(PasswordPolicy.FORCE_EXPIRED_ID);


        realms.forEach(realm -> {
            String passwordPolicy = realm.getPasswordPolicy();
            var charNumbs = PasswordPolicy.FORCE_EXPIRED_ID.length() + 3;
            var index = passwordPolicy.indexOf(PasswordPolicy.FORCE_EXPIRED_ID);
            var expirePolicy = passwordPolicy.substring(index, index + charNumbs);
            int expiresDays = Integer.parseInt(expirePolicy.substring(expirePolicy.indexOf('(') + 1, expirePolicy.lastIndexOf(')')));
            if(expiresDays != -1) {
                long timeToExpire = TimeUnit.DAYS.toMillis(expiresDays);
                policyRepository.findExpiredPasswords(realm.getId(), timeToExpire);
            }
        });
        log.info("stop: {}", DEBUG_STR);
    }

    @PostConstruct
    public void init() {
        this.host = properties.getProperty("application.host");
    }
}
