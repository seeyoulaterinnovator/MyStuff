package ru.alamics.sso.keycloak.registration.mapper;

import org.keycloak.models.UserModel;
import ru.alamics.sso.registration.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserModelUserMapper {

    private static final String PHONE = "phone";
    private static final String PHONE_VALIDATED_ON = "phone_validated_on";

    public User mapToUser(UserModel model) {
        return User.builder()
                .id(model.getId())
                .email(model.getEmail())
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .phone(model.getFirstAttribute(PHONE))
                .phoneVerifiedOn(model.getFirstAttribute(PHONE_VALIDATED_ON) != null ? LocalDateTime.parse(model.getFirstAttribute(PHONE_VALIDATED_ON)) : null)
                .attributes(model
                        .getAttributes()
                        .entrySet()
                        .stream()
                        .filter(stringListEntry -> !(stringListEntry.getKey().equals(PHONE) || stringListEntry.getKey().equals(PHONE_VALIDATED_ON)))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
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
        model.setAttribute(PHONE, List.of(user.getPhone()));
        model.setAttribute(PHONE_VALIDATED_ON, List.of(user.getPhoneVerifiedOn().toString()));

    }



}
