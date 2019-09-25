package ru.alamics.sso.credential;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.PasswordPolicy;
import ru.alamics.sso.keycloak.repository.PolicyRepository;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
@Slf4j
public class PasswordExpiresSchedule {

    @EJB
    private PolicyRepository policyRepository;

    @Schedule(hour = "*/3", persistent = false)
    public void findExpiredPassword() {
        final String DEBUG_STR = "findExpiredPassword";
        log.info("start: {}", DEBUG_STR);
        var realms = policyRepository.findRealmWithPolicy(PasswordPolicy.FORCE_EXPIRED_ID);
        realms.forEach(realm -> {
            String passwordPolicy = realm.getPasswordPolicy();
            var charNumbs = PasswordPolicy.FORCE_EXPIRED_ID.length() + 3;
            var index = passwordPolicy.indexOf(PasswordPolicy.FORCE_EXPIRED_ID);
            var expirePolicy = passwordPolicy.substring(index, index + charNumbs);
        });
        log.info("stop: {}", DEBUG_STR);
    }
}
