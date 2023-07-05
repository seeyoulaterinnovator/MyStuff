package ru.alamics.sso.remote.message;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocationBuilder;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import ru.alamics.sso.registration.phone.MsgConfig;
import ru.alamics.sso.registration.phone.exception.SendMessageException;
import ru.alamics.sso.registration.phone.model.MessageRequest;
import ru.alamics.sso.registration.phone.model.MessengerType;
import ru.alamics.sso.registration.phone.port.SendMessageService;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.StandResolver;
import ru.alamics.sso.util.Util;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static ru.alamics.sso.settings.SettingConstants.*;


@Slf4j
@Stateless(name = "MessageSender")
public class SendMessageServiceImpl implements SendMessageService {

    private static final ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS);
    private static final ResteasyClient client = clientBuilder.build();

    @Resource(lookup = "java:global/domru-sso/SettingsService")
    private SettingsService settingsService;

    public SendMessageServiceImpl() {
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
    public void sendMessageToMessengers(String phone, String message, String realmId, String[] messengerList) throws SendMessageException {
        String encodedMessage = null;
        String pattern = String.format("Your OTP is: %s.\n" +
                "@sso-balancer5.testing.srv.loc #%s", message, message);

        try {
            encodedMessage = URLEncoder.encode(pattern, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.info(e.getMessage());
            throw new SendMessageException();
        }

        MessageRequest messageRequest = MessageRequest.builder()
                .userPhone(phone)
                .text(encodedMessage)
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
        if (!StandResolver.isBattle()) {
            log.info("Stand {}, do not sending sms", StandResolver.ENV);
            return "0: Accepted for delivery";
        }

        MsgConfig msgConfig = createMsgConfig(messageRequest.getRealmId(), messageRequest.getMessengerName().getType());

        URI uri = msgConfig.getUrl();

        log.info(String.format("Api %s, Sending %s code to number: %s", uri.getHost(), messageRequest.getMessengerName().toString(), messageRequest.getUserPhone()));

        ClientInvocationBuilder builder = (ClientInvocationBuilder) client.register(StringTextStar.class)
                .target(uri)
                .queryParams(msgConfig.getConfigForQuery())
                .queryParam("to", Util.getCleanUserPhone(messageRequest.getUserPhone()))
               // .queryParam("text", Util.encodeCharset(messageRequest.getText(), msgConfig.getCharset()).replaceAll("%2B", "%20"))
                .queryParam("text", "bla%20bla%20bla")
                .request();
        try {
            return builder.get(String.class);
        } catch (ProcessingException | WebApplicationException wae) {
            log.error(wae.getMessage(), wae);
            throw new SendMessageException(wae);
        }
    }

    private MsgConfig createMsgConfig(String realmId, String type) {

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
