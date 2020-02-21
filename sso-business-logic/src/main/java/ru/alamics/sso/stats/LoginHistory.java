package ru.alamics.sso.stats;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.jpa.entity.UserLoginHistory;
import ru.alamics.sso.jpa.repository.UserHistoryLoginRepository;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.core.Context;
import java.time.LocalDateTime;

@Slf4j
@Stateless
public class LoginHistory {

    @EJB
    private UserHistoryLoginRepository repository;
    @Context
    private KeycloakSession session;

    public void create(UserEntity user) {
        UserLoginHistory history = UserLoginHistory.builder()
                .loginedAt(LocalDateTime.now())
                .user(user)
                .build();
        UserLoginHistory loginHistory = repository.save(history);

    }

}
