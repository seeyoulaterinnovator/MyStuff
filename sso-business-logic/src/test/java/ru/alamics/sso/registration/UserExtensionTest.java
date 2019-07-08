package ru.alamics.sso.registration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.models.UserModel;

import java.util.Map;

import static org.mockito.Mockito.mock;

class UserExtensionTest {

    private static UserExtension extension;

    @BeforeAll
    static void initAll() {
        extension = new UserExtension();
    }

    @Test
    void extendUser() {
        extension.extendUser(mock(UserModel.class), Map.of());


    }
}