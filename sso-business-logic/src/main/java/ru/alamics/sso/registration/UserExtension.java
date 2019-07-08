package ru.alamics.sso.registration;

import org.keycloak.models.UserModel;

import java.util.Map;

public class UserExtension {

    public void extendUser(UserModel user, Map<String, String> attributes) {
        for (Map.Entry<String, String> attributeValue :
                attributes.entrySet()) {
            user.setSingleAttribute(attributeValue.getKey(), attributeValue.getValue());
        }
    }

}
