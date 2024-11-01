package ru.alamics.sso.remote.message;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import ru.alamics.sso.registration.phone.MsgConfig;
import ru.alamics.sso.registration.phone.exception.SendMessageException;
import ru.alamics.sso.registration.phone.model.MessageRequest;
import ru.alamics.sso.registration.phone.model.MessengerType;
import ru.alamics.sso.registration.phone.port.SendMessageService;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.E2EUtil;
import ru.alamics.sso.util.StandResolver;
import ru.alamics.sso.util.Util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static ru.alamics.sso.settings.SettingConstants.*;

@ApplicationScoped
@Named("MessageSender")
@Slf4j
public class SendMessageServiceImpl implements SendMessageService {
    private final HttpClient client;

    @Inject
    SettingsService settingsService;

    public SendMessageServiceImpl(
            @Named("smsSenderSSLContext") SSLContext sslContext,
            @Named("smsSenderHostnameVerifier") HostnameVerifier hostnameVerifier
    ) {
        client = HttpClients.custom()
                .setSSLContext(sslContext)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(3_000)
                        .setConnectionRequestTimeout(3_000)
                        .setSocketTimeout(10_000)
                        .build())
                .build();
    }

    public String sendMessageByRequestAndLogInfo(MessageRequest messageRequest) throws SendMessageException {
        String response = sendMessageByRequest(messageRequest);
        String id = UUID.randomUUID().toString();
        String phone = messageRequest.getUserPhone();
        String text = messageRequest.getText();
        String messengerName = messageRequest.getMessengerName().toString();

        log.info("Sent {} to phone: {}, text: {}, id: {}, resp: {}", messengerName, phone, text, id, response);
        return response;
    }

    @Override
    public void sendMessageToMessengers(String phone, String message, String realmId, String[] messengerList, String host) throws SendMessageException {
//        без дополнительных данных в смс("Your OTP is..." не срабатывает автоподстановка кода из смс)
//        String pattern = String.format("Your OTP is: %s.\n\n" + "@%s #%s", message, host, message);

        String pattern = message;

        MessageRequest messageRequest = MessageRequest.builder()
                .userPhone(phone)
                .text(pattern)
                .realmId(realmId)
                .build();

        for (String messenger : messengerList) {
            messageRequest.setMessengerName(MessengerType.valueOf(messenger));
            sendMessageByRequestAndLogInfo(messageRequest);
        }
    }

    @Override
    public String sendMessageByRequest(MessageRequest messageRequest) throws SendMessageException {

        // локально и на дэве фиксированный код и не отправляю смс
        if (!StandResolver.isBattle() && !E2EUtil.isE2E()) {
            log.info("Stand {}, do not sending sms", StandResolver.ENV);
            return "0: Accepted for delivery";
        }

        MsgConfig msgConfig = createMsgConfig(messageRequest.getRealmId(), messageRequest.getMessengerName().getType());

        URI uri = msgConfig.getUrl();

        log.info(String.format("Api %s, Sending %s code to number: %s", uri.getHost(), messageRequest.getMessengerName().toString(), messageRequest.getUserPhone()));

        var uriBuilder = UriBuilder.fromUri(uri);
        msgConfig.getConfigForQuery().forEach((name, objects) -> uriBuilder.queryParam(name, objects.toArray()));

        HttpGet request = new HttpGet(
                uriBuilder.queryParam("to", Util.getCleanUserPhone(messageRequest.getUserPhone()))
                        .queryParam("text", Util.rfc3986Encoder(messageRequest.getText()))
                        .build()
        );
        request.setHeader(HttpHeaders.ACCEPT, MediaType.WILDCARD);
        try {
            HttpResponse response = client.execute(request);
            var status = response.getStatusLine().getStatusCode();
            if(status == HttpStatus.SC_OK || status == HttpStatus.SC_ACCEPTED) {
                try(InputStream stream = response.getEntity().getContent()) {
                    return new String(stream.readAllBytes());
                }
            }
            throw new SendMessageException(response.getStatusLine().getReasonPhrase());
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            throw new SendMessageException(e.getMessage(), e);
        }
    }

    protected MsgConfig createMsgConfig(String realmId, String type) {

        return MsgConfig.builder()
                .url(URI.create(settingsService.getSettingsStringValue(type + SEND_URI.getKey(), realmId)))
                .msgCenterName(settingsService.getSettingsStringValue(type + SMSC_NAME.getKey(), realmId))
                .username(settingsService.getSettingsStringValue(type + USERNAME_SENDER.getKey(), realmId))
                .password(settingsService.getSettingsStringValue(type + PASSWORD.getKey(), realmId))
                .senderName(settingsService.getSettingsStringValue(type + SENDER_NAME.getKey(), realmId))
                .timeout(settingsService.getSettingsIntegerValue(type + TIMEOUT.getKey(), realmId, 1440, "MsgSendServiceImpl: default value used: '%s' = '%s'"))
                .priority(MsgConfig.Priority.LOWEST)
                .reportsMask(MsgConfig.ReportsConfig.DELIVERED_TO_PHONE)
                .encoding(MsgConfig.Encoding.UCS2)
                .charset(StandardCharsets.UTF_8)
                .build();
    }

}
