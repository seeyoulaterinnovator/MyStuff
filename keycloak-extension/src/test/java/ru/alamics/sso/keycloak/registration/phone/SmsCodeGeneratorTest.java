package ru.alamics.sso.keycloak.registration.phone;

import org.junit.jupiter.api.Test;
import ru.alamics.sso.registration.phone.SmsCodeGenerator;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmsCodeGeneratorTest {

    @Test
    void getCode() {
        IntStream.range(1, 18).forEach(length -> {
            SmsCodeGenerator generator = new SmsCodeGenerator(length);
            assertThat(generator.getCode()).hasSize(length);
        });

        assertThatThrownBy(() -> new SmsCodeGenerator(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SmsCodeGenerator(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SmsCodeGenerator(19)).isInstanceOf(IllegalArgumentException.class);
    }
}