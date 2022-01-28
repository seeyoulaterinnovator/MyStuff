package ru.alamics.sso.remote.viber;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.phone.SmsConfig;
import ru.alamics.sso.registration.phone.exception.ViberSendException;
import ru.alamics.sso.registration.phone.port.ViberSendService;
import ru.alamics.sso.util.StandResolver;
import ru.alamics.sso.util.Util;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Stateless(name = "ViberSender")
public class ViberSendServiceImpl implements ViberSendService {
    private static final String SEND_URI = "viberSender.uri";
    private static final String SMSC_NAME = "viberSender.smscName";
    private static final String USERNAME = "viberSender.username";
    private static final String PASSWORD = "viberSender.password";
    private static final String SENDER_NAME = "viberSender.senderName";
    private static final String TIMEOUT = "viberSender.timeout";
    private static final ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS);
    private static final ResteasyClient client = clientBuilder.build();
    private static SmsConfig smsConfig;
    @Resource(lookup = "java:global/domru-sso/ApplicationProperties")
    private ApplicationProperties properties;

    public ViberSendServiceImpl() {
    }

    public ViberSendServiceImpl(SmsConfig config) {
        smsConfig = config;
    }

    @PostConstruct
    private void init() {
        smsConfig = SmsConfig.builder()
                .url(URI.create(properties.getProperty(SEND_URI)))
                .smsCenterName(properties.getProperty(SMSC_NAME))
                .username(properties.getProperty(USERNAME))
                .password(properties.getProperty(PASSWORD))
                .senderName(properties.getProperty(SENDER_NAME))
                .timeout(properties.getPropertyInt(TIMEOUT, 5, "ViberSendServiceImpl: default value used: '%s' = '%s'"))
                .priority(SmsConfig.Priority.HIGH)
                .reportsMask(SmsConfig.ReportsConfig.DELIVERED_TO_PHONE)
                .encoding(SmsConfig.Encoding.UCS2)
                .charset(StandardCharsets.UTF_8)
                .build();
    }

    @Override
    public String sendMsg(String phone, String text) throws ViberSendException {

        // локально и на дэве фиксированный код и не отправляю смс
        if (!StandResolver.isBattle()) {
            log.info("Stand {}, do not sending viber msg", StandResolver.ENV);
            return "0: Accepted for delivery";
        }

        URI uri = smsConfig.getUrl();

        log.info(String.format("Api %s, Sending VIBER code to number: %s", uri.getHost(), phone));

        try {
            ResteasyWebTarget webTarget = client.target(uri)
                    .queryParams(getConfigForQuery(smsConfig))
                    .queryParam("to", Util.getCleanUserPhone(phone))
                    .queryParam("text", Util.encodeCharset(text, smsConfig.getCharset()));

            String response = webTarget
                    .request()
                    .get(String.class);

            return response;

        } catch (ProcessingException | WebApplicationException wae) {
            log.error(wae.getMessage(), wae);

            throw new ViberSendException(wae);
        }
    }
}
