package ru.alamics.sso.keycloak.registration.mapper;

import org.keycloak.models.UserModel;
import ru.alamics.sso.registration.model.User;

import java.util.List;
import java.util.Map;

public class UserModelUserMapper {

    public User mapToUser(UserModel model) {
        return User.builder()
                .id(model.getId())
                .email(model.getEmail())
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .attributes(model.getAttributes())
                .build();
    }

    public void mergeUserInto(User user, UserModel model) {

        model.setEmail(user.getEmail());
        model.setFirstName(user.getFirstName());
        model.setLastName(user.getLastName());
        for (Map.Entry<String, List<String>> attributeEntry:
            user.getAttributes().entrySet()) {
            model.setAttribute(attributeEntry.getKey(), attributeEntry.getValue());
        }

    }



}
