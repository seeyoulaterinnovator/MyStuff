package ru.alamics.sso.remote.message;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocationBuilder;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import ru.alamics.sso.registration.phone.SmsConfig;
import ru.alamics.sso.registration.phone.exception.SendMessageException;
import ru.alamics.sso.registration.phone.model.MessageRequest;
import ru.alamics.sso.registration.phone.port.SendMessageService;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.StandResolver;
import ru.alamics.sso.util.Util;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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

    @Override
    public String sendSms(MessageRequest messageRequest) throws SendMessageException {

        // локально и на дэве фиксированный код и не отправляю смс
        if (!StandResolver.isBattle()) {
            log.info("Stand {}, do not sending sms", StandResolver.ENV);
            return "0: Accepted for delivery";
        }

        SmsConfig smsConfig = createSmsConfig(messageRequest.getRealmId(), messageRequest.getMessengerName().getType());

        URI uri = smsConfig.getUrl();

        log.info(String.format("Api %s, Sending %s code to number: %s", uri.getHost(), messageRequest.getMessengerName().toString(), messageRequest.getUserPhone()));

        ClientInvocationBuilder builder = (ClientInvocationBuilder) client.register(StringTextStar.class)
                .target(uri)
                .queryParams(smsConfig.getConfigForQuery())
                .queryParam("to", Util.getCleanUserPhone(messageRequest.getUserPhone()))
                .queryParam("text", Util.encodeCharset(messageRequest.getText(), smsConfig.getCharset()))
                .request();
        try {
            return builder.get(String.class);

        } catch (ProcessingException | WebApplicationException wae) {
            log.error(wae.getMessage(), wae);

            throw new SendMessageException(wae);
        }
    }

    private SmsConfig createSmsConfig(String realmId, String type) {

        return SmsConfig.builder()
                .url(URI.create(settingsService.getSettingsStringValue(type + SEND_URI.getKey(), realmId)))
                .smsCenterName(settingsService.getSettingsStringValue(type + SMSC_NAME.getKey(), realmId))
                .username(settingsService.getSettingsStringValue(type + USERNAME_SENDER.getKey(), realmId))
                .password(settingsService.getSettingsStringValue(type + PASSWORD.getKey(), realmId))
                .senderName(settingsService.getSettingsStringValue(type + SENDER_NAME.getKey(), realmId))
                .timeout(settingsService.getSettingsIntegerValue(type + TIMEOUT.getKey(), realmId, 1440, "SmsSendServiceImpl: default value used: '%s' = '%s'"))
                .priority(SmsConfig.Priority.LOWEST)
                .reportsMask(SmsConfig.ReportsConfig.DELIVERED_TO_PHONE)
                .encoding(SmsConfig.Encoding.UCS2)
                .charset(StandardCharsets.UTF_8)
                .build();
    }

}
