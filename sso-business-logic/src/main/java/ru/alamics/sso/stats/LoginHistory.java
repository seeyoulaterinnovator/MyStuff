package ru.alamics.sso.stats;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.UserLoginHistory;
import ru.alamics.sso.keycloak.repository.UserHistoryLoginRepository;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.time.LocalDateTime;

@Slf4j
@Stateless
public class LoginHistory {

    @EJB
    private UserHistoryLoginRepository repository;

    public void create(UserEntity user) {
        var history = UserLoginHistory.builder()
                .loginedAt(LocalDateTime.now())
                .user(user)
                .build();

        repository.save(history);
    }
}
