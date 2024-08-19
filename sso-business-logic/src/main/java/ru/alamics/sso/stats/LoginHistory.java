package ru.alamics.sso.stats;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.jpa.entity.UserLoginHistory;
import ru.alamics.sso.jpa.repository.UserHistoryLoginRepository;

import java.time.LocalDateTime;

@ApplicationScoped
@Slf4j
public class LoginHistory {

    @Inject
    UserHistoryLoginRepository repository;

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
