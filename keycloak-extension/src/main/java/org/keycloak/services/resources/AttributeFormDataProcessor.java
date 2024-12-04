package org.keycloak.services.resources;

import jakarta.ws.rs.core.MultivaluedMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.keycloak.authentication.requiredactions.util.UpdateProfileContext;
import org.keycloak.authentication.requiredactions.util.UserUpdateProfileContext;
import org.keycloak.models.Constants;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AttributeFormDataProcessor {
    public static void process(MultivaluedMap<String, String> formData, UpdateProfileContext user) {
        for (String key : formData.keySet()) {
            if (!key.startsWith(Constants.USER_ATTRIBUTES_PREFIX)) continue;
            String attribute = key.substring(Constants.USER_ATTRIBUTES_PREFIX.length());

            List<String> modelValue = new ArrayList<>(user.getAttributeStream(attribute).toList());

            int index = 0;
            for (String value : formData.get(key)) {
                addOrSetValue(modelValue, index, value);
                index++;
            }

            user.setAttribute(attribute, modelValue);
        }
    }

    public static void process(MultivaluedMap<String, String> formData, RealmModel realm, UserModel user) {
        process(formData, new UserUpdateProfileContext(realm, user));
    }

    private static void addOrSetValue(List<String> list, int index, String value) {
        if (list.size() > index) {
            list.set(index, value);
        } else {
            list.add(value);
        }
    }
}
