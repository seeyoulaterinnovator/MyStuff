package ru.alamics.sso.remote.sms;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.registration.phone.SmsConfig;
import ru.alamics.sso.registration.phone.exception.SmsSendException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class SmsSendServiceImplTest {

    public static final String USERNAME = "kannel_user";
    public static final String PASSWORD = "kannel_user";
    public static final String SENDER_NAME = "kannel_user";
    public static final String SMSC_NAME = "smsc_name";
    public static final String PHONE = "89824699045";
    public static final String TEXT = "test";
    public static final String REALM_ID = "user";
    public static final String PATH = "/cgi-bin/sendsms";
    private static WireMockServer server;

    private static SmsSendServiceImpl service;

    @BeforeAll
    static void initWireMock() {
        server = new WireMockServer(wireMockConfig().dynamicPort());
        server.start();

        SmsConfig.builder()
                .url(new ResteasyUriBuilder()
                        .scheme("http")
                        .host("127.0.0.1")
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
        service = new SmsSendServiceImpl();
    }

    @AfterEach
    void resetWireMock() {
        server.resetMappings();
    }

    @Test
    void sensSms() {

        Map<String, StringValuePattern> map = new HashMap<>();
        map.put("smsc", equalTo(SMSC_NAME));
        map.put("username", equalTo(USERNAME));
        map.put("password", equalTo(PASSWORD));
        map.put("from", equalTo(SENDER_NAME));
        map.put("validity", equalTo(String.valueOf(5)));
        map.put("priority", equalTo(String.valueOf(SmsConfig.Priority.HIGH.getPriorityAsInt())));
        map.put("dlr-mask", equalTo(String.valueOf(SmsConfig.ReportsConfig.DELIVERED_TO_PHONE)));
        map.put("coding", equalTo(String.valueOf(SmsConfig.Encoding.UCS2.getPriorityAsInt())));
        map.put("charset", equalTo(StandardCharsets.UTF_8.name()));

        server.stubFor(post(urlPathEqualTo(PATH))
                .withQueryParams(map)
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "TEXT/PLAIN")
                        .withBody("0: Accepted for delivery")
                )
        );
        try {
            String result = service.sendSms(PHONE, TEXT, REALM_ID);

            assertThat(result.substring(0, 1)).isEqualTo("0");

        } catch (SmsSendException e) {
            System.out.println(e);
        }
    }
}