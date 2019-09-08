package ru.alamics.sso.keycloak.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.UserLoginHistory;
import ru.alamics.sso.keycloak.repository.UserHistoryLoginRepository;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class UserHistoryLoginRepositoryImpl implements UserHistoryLoginRepository {

    private EntityManager em;

    public UserHistoryLoginRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<UserEntity> findInactiveUsers(final long absenceDays) {
        final String DEBUG_STR = "findInactiveUsers";
        log.debug("{}:", DEBUG_STR);
        LocalDate now = LocalDate.now();
        LocalDate absence = now.minusDays(absenceDays);

        List<UserEntity> ret = em.createQuery("select distinct ul from UserLoginHistory ul join ul.user user where ul.loginedAt <=:time " +
                "order by ul.loginedAt desc", UserLoginHistory.class)
                .setParameter("time", absence.atTime(LocalTime.MIN))
                .getResultStream()
                .map(UserLoginHistory::getUser)
                .collect(Collectors.toList());

        return ret;
    }

    @Override
    public UserLoginHistory save(UserLoginHistory history) {
        final String id = UUID.randomUUID().toString();
        history.setId(id);
        em.persist(history);
        em.flush();

        return history;
    }

}
