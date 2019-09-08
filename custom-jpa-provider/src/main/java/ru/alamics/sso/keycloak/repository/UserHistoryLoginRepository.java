package ru.alamics.sso.keycloak.repository;

import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.UserLoginHistory;

import java.util.List;

public interface UserHistoryLoginRepository extends Repository {
    UserLoginHistory save(UserLoginHistory loginHistory);
    List<UserEntity> findInactiveUsers(final long absenceDays);
}
