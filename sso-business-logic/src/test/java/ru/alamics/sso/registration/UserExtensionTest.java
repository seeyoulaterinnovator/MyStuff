package ru.alamics.sso.registration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.registration.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserExtensionTest {

    private static UserExtension extension;

    @BeforeAll
    static void initAll() {
        extension = new UserExtension();
    }

    @Test
    void extendUser() {

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .email("test@test.test")
                .name("Test")
                .build();

        Map<String, Object> map = new HashMap<>();
        map.put("clientId", "someClientId");
        map.put("someAttribute", "someValue");

        extension.extendUser(user, map);

        Map<String, List<String>> attributes = user.getAttributes();

        assertThat(attributes).containsKeys("clientId", "someAttribute");


    }
}