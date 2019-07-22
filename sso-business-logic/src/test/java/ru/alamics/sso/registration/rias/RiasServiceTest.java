package ru.alamics.sso.registration.rias;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.rias.port.RiasApiService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RiasServiceTest {

    private static RiasApiService riasApiService ;
    private static RiasService service;

    private static final String PHONE = "89999999999";
    private static final String EMAIL = "test@test.test";

    @BeforeAll
    static void init() {
        riasApiService = mock(RiasApiService.class);
        service = new RiasService(riasApiService);
    }

    @Test
    void checkUserEmailExists() {
        when(riasApiService.checkParam(eq(EMAIL))).thenReturn(true);

        User user = User.builder()
                .email(EMAIL)
                .build();

        boolean isExists = service.checkEmail(user);

        assertThat(isExists).isTrue();

    }

    @Test
    void checkUserPhoneExists() {
        when(riasApiService.checkParam(eq(PHONE))).thenReturn(true);

        User user = User.builder()
                .phone(PHONE)
                .build();

        boolean isExists = service.checkPhone(user);

        assertThat(isExists).isTrue();

    }

    @Test
    void checkUserNotExists() {
        when(riasApiService.checkParam(eq(EMAIL))).thenReturn(false);
        when(riasApiService.checkParam(eq(PHONE))).thenReturn(false);

        User user = User.builder()
                .phone(PHONE)
                .email(EMAIL)
                .build();

        boolean emailCheck = service.checkEmail(user);
        boolean phoneCheck = service.checkPhone(user);

        assertThat(emailCheck).isFalse();
        assertThat(phoneCheck).isFalse();

    }
}