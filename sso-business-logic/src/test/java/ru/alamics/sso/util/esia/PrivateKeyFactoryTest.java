package ru.alamics.sso.util.esia;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public final class PrivateKeyFactoryTest {

    @Test
    public void nonPrivateKey() throws IOException {
        Path path = Paths.get("src/test/resources/cert/client-certificates-1.pem");

        try {
            PrivateKeyFactory.generate(path);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageStartingWith(String.format("%s contains an artifact that is not a key pair: ", path));
        }
    }

    @Test
    public void test() throws IOException {
        assertThat(PrivateKeyFactory.generate(Paths.get("src/test/resources/cert/private.pem"))).isNotNull();
    }

}