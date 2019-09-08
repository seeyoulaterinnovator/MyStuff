package ru.alamics.sso.stats;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.UserLoginHistory;
import ru.alamics.sso.keycloak.factory.RepositoryFactory;
import ru.alamics.sso.keycloak.repository.UserHistoryLoginRepository;

import java.time.LocalDateTime;

@Slf4j
public class LoginHistory {

    private UserHistoryLoginRepository repository;
    private KeycloakSession session;

    public LoginHistory(KeycloakSession session) {
        this.session = session;
        this.repository = RepositoryFactory.create(session);
    }

    public void create(UserEntity user) {
        var history = UserLoginHistory.builder()
                .loginedAt(LocalDateTime.now())
                .user(user)
                .build();
        UserLoginHistory loginHistory = repository.save(history);

    }

}
