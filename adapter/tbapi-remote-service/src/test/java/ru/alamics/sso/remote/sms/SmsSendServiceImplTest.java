package ru.alamics.sso.remote.sms;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.registration.phone.SmsConfig;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class SmsSendServiceImplTest {

    public static final String USERNAME = "kannel_user";
    public static final String PASSWORD = "kannel_user";
    public static final String SENDER_NAME = "kannel_user";
    public static final String SMSC_NAME = "smsc_name";
    public static final String PHONE = "89824699045";
    public static final String TEXT = "test";
    public static final String PATH = "/cgi-bin/sendsms";
    private static WireMockServer server;

    private static SmsSendServiceImpl service;

    @BeforeAll
    static void initWireMock() {
        server = new WireMockServer(wireMockConfig().dynamicPort());
        server.start();

        SmsConfig smsConfig = SmsConfig.builder()
                .url(new ResteasyUriBuilder()
                        .scheme("http")
                        .host("localhost")
                        .port(server.port())
                        .path(PATH)
                        .build())
                .smsCenterName(SMSC_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .senderName(SENDER_NAME)
                .timeout(5)
                .priority(SmsConfig.Priority.HIGH)
                .reportsMask(SmsConfig.ReportsConfig.DELIVERED_TO_PHONE)
                .encoding(SmsConfig.Encoding.UCS2)
                .charset(StandardCharsets.UTF_8)
                .build();
        service = new SmsSendServiceImpl(smsConfig);
    }

    @AfterEach
    void resetWireMock() {
        server.resetMappings();
    }
    @Test
    void sensSms() {

        server.stubFor(post(urlPathEqualTo(PATH))
                .withQueryParams(Map.of(
                        "smsc", equalTo(SMSC_NAME),
                        "username", equalTo(USERNAME),
                        "password", equalTo(PASSWORD),
                        "from", equalTo(SENDER_NAME),
                        "validity", equalTo(String.valueOf(5)),
                        "priority", equalTo(String.valueOf(SmsConfig.Priority.HIGH.getPriorityAsInt())),
                        "dlr-mask", equalTo(String.valueOf(SmsConfig.ReportsConfig.DELIVERED_TO_PHONE)),
                        "coding", equalTo(String.valueOf(SmsConfig.Encoding.UCS2.getPriorityAsInt())),
                        "charset", equalTo(StandardCharsets.UTF_8.name())
                ))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "TEXT/PLAIN")
                        .withBody("0: Accepted for delivery")
                )
        );

        Integer result = service.sendSms(PHONE, TEXT);

        assertThat(result).isEqualTo(0);

    }
}