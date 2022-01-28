package ru.alamics.sso.remote.message;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocationBuilder;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import ru.alamics.sso.registration.phone.SmsConfig;
import ru.alamics.sso.registration.phone.exception.SendMessageExceprion;
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
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
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
    public String sendSms(MessageRequest messageRequest) throws SendMessageExceprion {

        // локально и на дэве фиксированный код и не отправляю смс
        if (!StandResolver.isBattle()) {
            log.info("Stand {}, do not sending sms", StandResolver.ENV);
            return "0: Accepted for delivery";
        }

        SmsConfig smsConfig = createSmsConfig(messageRequest.getRealmId(), messageRequest.getMessengerName());

        URI uri = smsConfig.getUrl();

        log.info(String.format("Api %s, Sending %s code to number: %s", uri.getHost(), messageRequest.getMessengerName().getType(), messageRequest.getUserPhone()));


        ClientInvocationBuilder builder = (ClientInvocationBuilder) client.register(StringTextStar.class)
                .target(uri)
                .queryParams(getConfigForQuery(smsConfig))
                .queryParam("to", Util.getCleanUserPhone(messageRequest.getUserPhone()))
                .queryParam("text", Util.encodeCharset(messageRequest.getText(), smsConfig.getCharset()))
                .request();
        try {
            return builder.get(String.class);

        } catch (ProcessingException | WebApplicationException wae) {
            log.error(wae.getMessage(), wae);

            throw new SendMessageExceprion(wae);
        }
    }

    private SmsConfig createSmsConfig(String realmId, MessengerType type) {
        
        SmsConfig smsConfig;

        switch (type) {
            case SMS:
                smsConfig = SmsConfig.builder()
                    .url(URI.create(settingsService.getSettingsStringValue(SEND_URI_SMS, realmId)))
                    .smsCenterName(settingsService.getSettingsStringValue(SMSC_NAME_SMS, realmId))
                    .username(settingsService.getSettingsStringValue(USERNAME_SMS, realmId))
                    .password(settingsService.getSettingsStringValue(PASSWORD_SMS, realmId))
                    .senderName(settingsService.getSettingsStringValue(SENDER_NAME_SMS, realmId))
                    .timeout(settingsService.getSettingsIntegerValue(TIMEOUT_SMS, realmId, 1440, "SmsSendServiceImpl: default value used: '%s' = '%s'"))
                    .priority(SmsConfig.Priority.LOWEST)
                    .reportsMask(SmsConfig.ReportsConfig.DELIVERED_TO_PHONE)
                    .encoding(SmsConfig.Encoding.UCS2)
                    .charset(StandardCharsets.UTF_8)
                    .build();
                break;
            case VIBER:
                smsConfig = SmsConfig.builder()
                        .smsCenterName(settingsService.getSettingsStringValue(SMSC_NAME_VIBER, realmId))
                        .username(settingsService.getSettingsStringValue(USERNAME_VIBER, realmId))
                        .password(settingsService.getSettingsStringValue(PASSWORD_VIBER, realmId))
                        .senderName(settingsService.getSettingsStringValue(SENDER_NAME_VIBER, realmId))
                        .timeout(settingsService.getSettingsIntegerValue(TIMEOUT_VIBER, realmId, 1440, "ViberSendServiceImpl: default value used: '%s' = '%s'"))
                        .priority(SmsConfig.Priority.LOWEST)
                        .reportsMask(SmsConfig.ReportsConfig.DELIVERED_TO_PHONE)
                        .encoding(SmsConfig.Encoding.UCS2)
                        .charset(StandardCharsets.UTF_8)
                        .build();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
        
        return smsConfig;

    }

    private MultivaluedMap<String, Object> getConfigForQuery(SmsConfig smsConfig) {

        MultivaluedHashMap<String, Object> map = new MultivaluedHashMap<>();
        map.add("smsc", smsConfig.getSmsCenterName());
        map.add("username", smsConfig.getUsername());
        map.add("password", smsConfig.getPassword());
        map.add("from", smsConfig.getSenderName());
        map.add("validity", smsConfig.getTimeout());
        map.add("priority", smsConfig.getPriority().getPriorityAsInt());
        map.add("dlr-mask", smsConfig.getReportsMask());
        map.add("coding", smsConfig.getEncoding().getPriorityAsInt());
        map.add("charset", smsConfig.getCharset());

        return map;
    }
}
