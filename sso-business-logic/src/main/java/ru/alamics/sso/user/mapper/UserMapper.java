package ru.alamics.sso.user.mapper;

import org.keycloak.models.UserModel;
import ru.alamics.sso.user.web.UserDto;

public class UserMapper {

    public static UserDto toDto(UserModel model) {
        return UserDto.builder()
                .attributes(model.getAttributes())
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .email(model.getEmail())
                .id(model.getId())
                .username(model.getUsername())
                .build();
    }
}
