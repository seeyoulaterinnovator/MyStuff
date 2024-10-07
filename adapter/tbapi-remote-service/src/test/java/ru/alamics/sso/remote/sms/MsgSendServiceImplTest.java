package ru.alamics.sso.remote.sms;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import jakarta.ws.rs.core.UriBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.registration.phone.MsgConfig;
import ru.alamics.sso.registration.phone.exception.SendMessageException;
import ru.alamics.sso.registration.phone.model.MessageRequest;
import ru.alamics.sso.registration.phone.model.MessengerType;
import ru.alamics.sso.remote.message.SendMessageServiceImpl;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class MsgSendServiceImplTest {

    public static final String USERNAME = "kannel_user";
    public static final String PASSWORD = "kannel_user";
    public static final String SENDER_NAME = "kannel_user";
    public static final String SMSC_NAME = "smsc_name";
    public static final String PHONE = "89824699045";
    public static final String TEXT = "test";
    public static final String REALM_ID = "user";
    public static final MessengerType MESSENGER_TYPE = MessengerType.SMS;
    public static final String PATH = "/cgi-bin/sendsms";
    private static WireMockServer server;

    private static SendMessageServiceImpl service;

    @BeforeAll
    static void initWireMock() {
        server = new WireMockServer(wireMockConfig().dynamicPort());
        server.start();

        service = new SendMessageServiceImpl() {
            @Override
            protected MsgConfig createMsgConfig(String realmId, String type) {
                return MsgConfig.builder()
                        .url(UriBuilder.newInstance()
                                .scheme("http")
                                .host("127.0.0.1")
                                .port(server.port())
                                .path(PATH)
                                .build())
                        .msgCenterName(SMSC_NAME)
                        .username(USERNAME)
                        .password(PASSWORD)
                        .senderName(SENDER_NAME)
                        .timeout(5)
                        .priority(MsgConfig.Priority.HIGH)
                        .reportsMask(MsgConfig.ReportsConfig.DELIVERED_TO_PHONE)
                        .encoding(MsgConfig.Encoding.UCS2)
                        .charset(StandardCharsets.UTF_8)
                        .build();
            }
        };
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
        map.put("priority", equalTo(String.valueOf(MsgConfig.Priority.HIGH.getPriorityAsInt())));
        map.put("dlr-mask", equalTo(String.valueOf(MsgConfig.ReportsConfig.DELIVERED_TO_PHONE)));
        map.put("coding", equalTo(String.valueOf(MsgConfig.Encoding.UCS2.getPriorityAsInt())));
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
            MessageRequest messageRequest = MessageRequest.builder()
                    .userPhone(PHONE)
                    .messengerName(MESSENGER_TYPE)
                    .realmId(REALM_ID)
                    .text(TEXT)
                    .build();

            String result = service.sendMessageByRequest(messageRequest);

            assertThat(result.substring(0, 1)).isEqualTo("0");

        } catch (SendMessageException e) {
            System.out.println(e);
        }
    }
}
