package ru.alamics.sso.keycloak.repository;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.UserLoginHistory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Stateless
public class UserHistoryLoginRepository {

    @PersistenceContext
    private EntityManager em;


    public List<UserEntity> findInactiveUsers() {
        final String DEBUG_STR = "findInactiveUsers";
        log.debug("{}:", DEBUG_STR);
        LocalDate now = LocalDate.now();

        List<UserLoginHistory> ret = em.createQuery("select distinct ul from UserLoginHistory ul join ul.user user order by ul.loginedAt desc", UserLoginHistory.class)
                .getResultList();

        return null;
    }

    public UserLoginHistory save(UserLoginHistory history) {
        final String id = UUID.randomUUID().toString();
        history.setId(id);
        em.persist(history);
        em.flush();

        return history;
    }
}
