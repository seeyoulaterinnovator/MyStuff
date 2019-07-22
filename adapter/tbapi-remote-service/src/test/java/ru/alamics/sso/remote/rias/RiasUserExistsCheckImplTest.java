package ru.alamics.sso.remote.rias;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.assertj.core.api.Assertions;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.registration.phone.SmsConfig;
import ru.alamics.sso.remote.sms.SmsSendServiceImpl;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class RiasUserExistsCheckImplTest {

    private static WireMockServer server;

    private static RiasUserExistsCheckImpl service;

    public static final String PATH = "/cgi-bin/ppo/excells/web_cabinet.get_info_unauth";

    @BeforeAll
    static void initWireMock() {
        server = new WireMockServer(wireMockConfig().dynamicPort());
        server.start();

        service = new RiasUserExistsCheckImpl(new ResteasyUriBuilder()
                .scheme("http")
                .host("localhost")
                .port(server.port())
                .path(PATH)
                .build()
        );
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

        boolean result = service.checkParam("test@test.test");
        assertThat(result).isTrue();
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

        boolean result = service.checkParam("test@test.test");
        assertThat(result).isFalse();
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

        assertThatThrownBy(() -> service.checkParam("test@test.test")).isInstanceOf(RuntimeException.class).hasMessageContaining("SECRET_ERROR");
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

        assertThatThrownBy(() -> service.checkParam("test@test.test")).isInstanceOf(RuntimeException.class).hasMessageContaining("empty");
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

        assertThatThrownBy(() -> service.checkParam("test@test.test")).isInstanceOf(RuntimeException.class).hasMessageContaining("invalid");
    }


}