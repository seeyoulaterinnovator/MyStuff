package ru.alamics.sso.registration;

import ru.alamics.sso.registration.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserExtension {

    public static final int ATTR_FIELD_LEN = 255;

    public void extendUser(User user, Map<String, Object> attributes) {
        Map<String, List<String>> userAttributes = user.getAttributes();
        for (Map.Entry<String, Object> attributeEntry :
                attributes.entrySet()) {
            String key = attributeEntry.getKey();
            Object value = attributeEntry.getValue();
            if (value instanceof String) {
                userAttributes.compute(key, (s, strings) -> {
                    strings = strings == null ? new ArrayList<>() : strings;
                    strings.add(value == null? null : ((String) value).substring(0, ATTR_FIELD_LEN));
                    return strings;
                });
            }
        }
    }

}
