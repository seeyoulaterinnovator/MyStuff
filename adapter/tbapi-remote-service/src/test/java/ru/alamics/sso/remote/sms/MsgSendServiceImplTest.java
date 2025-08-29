package ru.alamics.sso.remote.sms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.ws.rs.core.UriBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.registration.phone.exception.SendMessageException;
import ru.alamics.sso.registration.phone.model.MessageRequest;
import ru.alamics.sso.registration.phone.model.MessengerType;
import ru.alamics.sso.remote.message.SendMessageServiceImpl;
import ru.alamics.sso.remote.message.SmsMessageSender;
import ru.alamics.sso.remote.message.SmsRequest;

import javax.net.ssl.SSLContext;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class MsgSendServiceImplTest {

    public static final String AUTH_TOKEN = "kannel_user";
    public static final String SIMULATE = "0";
    public static final String SENDER_NAME = "Domru";
    public static final String PHONE = "89824699045";
    public static final String TEXT = "test";
    public static final String REALM_ID = "user";
    public static final MessengerType MESSENGER_TYPE = MessengerType.SMS;
    public static final String PATH = "/message";
    private final static String VALID_RESPONSE = """
            {
              "requestId": "813cd8ec49548c764830bf72d16dad02"
            }
            """;
    private static WireMockServer server;

    private static SendMessageServiceImpl service;
    private static SmsMessageSender smsMessageSender;

    @BeforeAll
    static void initWireMock() throws Exception {
        server = new WireMockServer(wireMockConfig().dynamicPort());
        server.start();

        smsMessageSender = new SmsMessageSender(SSLContext.getDefault(), (s, ss) -> true) {
            @Override
            protected String getSetting(String key, String realmId) {
                String apiUrl = UriBuilder.newInstance()
                        .scheme("http")
                        .host("127.0.0.1")
                        .port(server.port())
                        .path(PATH)
                        .build()
                        .toString();

                Map<String, String> properties = Map.of(
                        "smsSender.uri", apiUrl,
                        "smsSender.senderName", SENDER_NAME,
                        "smsSender.simulate", SIMULATE,
                        "smsSender.authToken", AUTH_TOKEN
                );

                return properties.get(key);
            }

            @Override
            protected boolean isSmsSenderMocked() {
                return false;
            }
        };

        service = new SendMessageServiceImpl() {
            protected SmsMessageSender getSmsSender() {
                return smsMessageSender;
            }
        };
    }

    @AfterEach
    void resetWireMock() {
        server.resetMappings();
    }

    @Test
    void sensSms() throws JsonProcessingException {

        SmsRequest sms = new SmsRequest(PHONE, TEXT, SENDER_NAME, SIMULATE.equals("1"));
        String body = (new ObjectMapper()).writeValueAsString(sms);

        server.stubFor(post(urlPathEqualTo(PATH))
                .withRequestBody(equalTo(body))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/json")
                        .withBody(VALID_RESPONSE)
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

            assertThat(result).isEqualTo(VALID_RESPONSE);

        } catch (SendMessageException e) {
            System.out.println(e);
        }
    }
}
