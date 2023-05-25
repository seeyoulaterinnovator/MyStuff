package ru.alamics.sso.keycloak.util;

import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.registration.model.User;

public class UserToUserEntityMapper {
    public static UserEntity toUserEntity(User user) {
        //need just id
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        return entity;
    }
}
