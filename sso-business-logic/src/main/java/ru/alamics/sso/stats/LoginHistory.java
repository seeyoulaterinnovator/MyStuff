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
                .realm(user.getRealmId())
                .user(user)
                .build();
        repository.save(history);

    }

    public void createSuccessAuth(UserEntity entity, String realm) {
        UserLoginHistory history = UserLoginHistory.builder()
                .isSuccess(true)
                .realm(realm)
                .loginedAt(LocalDateTime.now())
                .user(entity)
                .build();
        repository.saveSuccessAuth(history);
    }

}
