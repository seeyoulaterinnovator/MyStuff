package ru.alamics.sso.registration;

import org.keycloak.models.UserModel;

import java.util.Map;

public class UserExtension {

    public void extendUser(UserModel user, Map<String, Object> attributes) {
        for (Map.Entry<String, Object> attributeValue :
                attributes.entrySet()) {
            Object value = attributeValue.getValue();
            if (value instanceof String) {
                user.setSingleAttribute(attributeValue.getKey(), (String) value);
            }
        }
    }

}
