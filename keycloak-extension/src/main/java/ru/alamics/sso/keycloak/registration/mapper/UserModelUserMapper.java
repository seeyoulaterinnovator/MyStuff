package ru.alamics.sso.keycloak.registration.mapper;

import jakarta.ws.rs.core.MultivaluedMap;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.UserModel;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.util.Util;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.alamics.sso.registration.model.FormConstants.FIELD_PHONE;
import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;
import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_VALIDATED_ON;

public class UserModelUserMapper {

    public static User mapToUser(UserModel model) {
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

    public static void mergeUserInto(User user, UserModel model) {
        model.setEmail(user.getEmail());
        model.setFirstName(user.getName());
        for (Map.Entry<String, List<String>> attributeEntry :
                user.getAttributes().entrySet()) {
            model.setAttribute(attributeEntry.getKey(), attributeEntry.getValue());
        }
        if (user.getPhone() != null) {

            String phone = user.getPhone();
            phone = Util.getCleanUserPhone(phone);

            model.setAttribute(ATTR_PHONE_NAME, Collections.singletonList(phone));
        }
        if (user.getPhoneVerifiedOn() != null)
            model.setAttribute(ATTR_PHONE_VALIDATED_ON, List.of(user.getPhoneVerifiedOn().toString()));

    }

    public static void fillAttributesFromContext(UserModel user, HttpRequest httpRequest) {
        MultivaluedMap<String, String> formData = httpRequest.getDecodedFormParameters();
        if (formData.getFirst(FIELD_PHONE) != null) {

            String phone = formData.getFirst(FIELD_PHONE);
            if (phone != null)
                phone = Util.getCleanUserPhone(phone);

            user.setAttribute(ATTR_PHONE_NAME, Collections.singletonList(phone));
        }

        if (formData.getFirst(ATTR_PHONE_VALIDATED_ON) != null) {
            user.setAttribute(ATTR_PHONE_VALIDATED_ON, Collections.singletonList(formData.getFirst(ATTR_PHONE_VALIDATED_ON)));
        }
    }
}
