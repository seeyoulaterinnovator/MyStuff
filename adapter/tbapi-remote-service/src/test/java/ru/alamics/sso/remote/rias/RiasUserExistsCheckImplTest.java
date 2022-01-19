package ru.alamics.sso.remote.rias;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.rias.exception.RiasCheckException;
import ru.alamics.sso.remote.ApplicationPropertiesMock;

import java.net.URI;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiasUserExistsCheckImplTest {

    public static final String PATH = "/cgi-bin/ppo/excells/web_cabinet.get_info_unauth";
    private static WireMockServer server;
    private static RiasUserExistsCheckImpl service;

    @BeforeAll
    static void initWireMock() {
        server = new WireMockServer(wireMockConfig().dynamicPort());
        server.start();

        URI uri = new ResteasyUriBuilder()
                .scheme("http")
                .host("localhost")
                .port(server.port())
                .path(PATH)
                .build();

        ApplicationProperties props = new ApplicationPropertiesMock(new Properties());

        service = new RiasUserExistsCheckImpl(props, uri);
    }

    @Test
    void checkParamUserFound() {
        server.stubFor(get(urlPathEqualTo(PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBodyFile("user_found.xml")
                )
        );

        try {
            boolean result = service.checkParam("test@test.test");
            assertThat(result).isTrue();

        } catch (RiasCheckException e) {

        }
    }

    @Test
    void checkParamUserNotFound() {
        server.stubFor(get(urlPathEqualTo(PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBodyFile("user_not_found.xml")
                )
        );

        try {
            boolean result = service.checkParam("test@test.test");
            assertThat(result).isFalse();
        } catch (RiasCheckException e) {

        }
    }

    @Test
    void checkParamWrongSecret() {
        server.stubFor(get(urlPathEqualTo(PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBodyFile("secret_error.xml")
                )
        );

        assertThatThrownBy(() -> service.checkParam("test@test.test")).isInstanceOf(RiasCheckException.class).hasMessageContaining("SECRET_ERROR");
    }

    @Test
    void checkParamWithEmptyData() {
        server.stubFor(get(urlPathEqualTo(PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBodyFile("empty_data.xml")
                )
        );

        assertThatThrownBy(() -> service.checkParam("test@test.test")).isInstanceOf(RiasCheckException.class).hasMessageContaining("empty");
    }

    @Test
    void checkParamWithInvalidParam() {
        server.stubFor(get(urlPathEqualTo(PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBodyFile("param_invalid.xml")
                )
        );

        assertThatThrownBy(() -> service.checkParam("test@test.test")).isInstanceOf(RiasCheckException.class).hasMessageContaining("invalid");
    }


}