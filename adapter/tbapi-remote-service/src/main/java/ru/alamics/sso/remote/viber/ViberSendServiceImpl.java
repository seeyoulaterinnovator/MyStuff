package ru.alamics.sso.remote.viber;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import ru.alamics.sso.registration.phone.SmsConfig;
import ru.alamics.sso.registration.phone.exception.ViberSendException;
import ru.alamics.sso.registration.phone.port.ViberSendService;
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
@Stateless(name = "ViberSender")
public class ViberSendServiceImpl implements ViberSendService {

    private static final ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS);
    private static final ResteasyClient client = clientBuilder.build();

    @Resource(lookup = "java:global/domru-sso/SettingsService")
    private SettingsService settingsService;

    public ViberSendServiceImpl() {
    }

    @Override
    public String sendMsg(String phone, String text, String realmId) throws ViberSendException {

        // локально и на дэве фиксированный код и не отправляю смс
        if (!StandResolver.isBattle()) {
            log.info("Stand {}, do not sending viber msg", StandResolver.ENV);
            return "0: Accepted for delivery";
        }

        SmsConfig smsConfig = createSmsConfig(realmId);

        URI uri = URI.create(settingsService.getSettingsStringValue(SEND_URI_VIBER, realmId));

        log.info(String.format("Api %s, Sending VIBER code to number: %s", uri.getHost(), phone));

        try {
            ResteasyWebTarget webTarget = client.target(uri)
                    .queryParams(getConfigForQuery(smsConfig))
                    .queryParam("to", Util.getCleanUserPhone(phone))
                    .queryParam("text", Util.encodeCharset(text, smsConfig.getCharset()));

            return webTarget
                    .request()
                    .get(String.class);

        } catch (ProcessingException | WebApplicationException wae) {
            log.error(wae.getMessage(), wae);

            throw new ViberSendException(wae);
        }
    }

    private SmsConfig createSmsConfig(String realmId) {
        return SmsConfig.builder()
                .smsCenterName(settingsService.getSettingsStringValue(SMSC_NAME_VIBER, realmId))
                .username(settingsService.getSettingsStringValue(USERNAME_VIBER, realmId))
                .password(settingsService.getSettingsStringValue(PASSWORD_VIBER, realmId))
                .senderName(settingsService.getSettingsStringValue(SENDER_NAME_VIBER, realmId))
                .timeout(settingsService.getSettingsIntegerValue(TIMEOUT_VIBER, realmId, 5, "SmsSendServiceImpl: default value used: '%s' = '%s'"))
                .priority(SmsConfig.Priority.HIGH)
                .reportsMask(SmsConfig.ReportsConfig.DELIVERED_TO_PHONE)
                .encoding(SmsConfig.Encoding.UCS2)
                .charset(StandardCharsets.UTF_8)
                .build();
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
