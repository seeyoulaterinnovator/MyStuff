package ru.alamics.sso.keycloak.registration.mapper;

import org.keycloak.models.UserModel;
import ru.alamics.sso.registration.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;
import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_VALIDATED_ON;

public class UserModelUserMapper {

    public User mapToUser(UserModel model) {
        return User.builder()
                .id(model.getId())
                .email(model.getEmail())
                .name(model.getFirstName())
                .phone(model.getFirstAttribute(ATTR_PHONE_NAME))
                .phoneVerifiedOn(model.getFirstAttribute(ATTR_PHONE_VALIDATED_ON) != null ? LocalDateTime.parse(model.getFirstAttribute(ATTR_PHONE_VALIDATED_ON)) : null)
                .attributes(model
                        .getAttributes()
                        .entrySet()
                        .stream()
                        .filter(stringListEntry -> !(stringListEntry.getKey().equals(ATTR_PHONE_NAME) || stringListEntry.getKey().equals(ATTR_PHONE_VALIDATED_ON)))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .build();
    }

    public void mergeUserInto(User user, UserModel model) {

        model.setEmail(user.getEmail());
        model.setFirstName(user.getName());
        for (Map.Entry<String, List<String>> attributeEntry:
            user.getAttributes().entrySet()) {
            model.setAttribute(attributeEntry.getKey(), attributeEntry.getValue());
        }
        if (user.getPhone() != null)
            model.setAttribute(ATTR_PHONE_NAME, List.of(user.getPhone()));
        if (user.getPhoneVerifiedOn() != null)
            model.setAttribute(ATTR_PHONE_VALIDATED_ON, List.of(user.getPhoneVerifiedOn().toString()));

    }



}
