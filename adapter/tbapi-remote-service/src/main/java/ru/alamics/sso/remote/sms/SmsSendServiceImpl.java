package ru.alamics.sso.remote.sms;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocationBuilder;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.phone.SmsConfig;
import ru.alamics.sso.registration.phone.exception.SmsSendException;
import ru.alamics.sso.registration.phone.port.SmsSendService;
import ru.alamics.sso.util.StandResolver;
import ru.alamics.sso.util.Util;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Stateless(name = "SmsSender")
public class SmsSendServiceImpl implements SmsSendService {
    @Resource(lookup = "java:global/domru-sso/ApplicationProperties")
    private ApplicationProperties properties;

    private static final String SEND_URI = "smsSender.uri";
    private static final String SMSC_NAME = "smsSender.smscName";
    private static final String USERNAME = "smsSender.username";
    private static final String PASSWORD = "smsSender.password";
    private static final String SENDER_NAME = "smsSender.senderName";
    private static final String TIMEOUT = "smsSender.timeout";

    private static final ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS);

    private static final ResteasyClient client = clientBuilder.build();

    private static SmsConfig smsConfig;

    @PostConstruct
    private void init() {
        smsConfig = SmsConfig.builder()
                .url(URI.create(properties.getProperty(SEND_URI)))
                .smsCenterName(properties.getProperty(SMSC_NAME))
                .username(properties.getProperty(USERNAME))
                .password(properties.getProperty(PASSWORD))
                .senderName(properties.getProperty(SENDER_NAME))
                .timeout(Integer.parseInt(properties.getProperty(TIMEOUT)))
                .priority(SmsConfig.Priority.LOWEST)
                .reportsMask(SmsConfig.ReportsConfig.DELIVERED_TO_PHONE)
                .encoding(SmsConfig.Encoding.UCS2)
                .charset(StandardCharsets.UTF_8)
                .build();
    }

    public SmsSendServiceImpl() {

    }

    public SmsSendServiceImpl(SmsConfig smsConfig) {
        this.smsConfig = smsConfig;
    }

    @Override
    public String sendSms(String phone, String text) throws SmsSendException {

        // локально и на дэве фиксированный код и не отправляю смс
        if (!StandResolver.isBattle()) {
            log.info("Stand {}, do not sending sms", StandResolver.ENV);
            return "0: Accepted for delivery";
        }

        URI uri = smsConfig.getUrl();

        // TODO https://stackoverflow.com/questions/53760939/processingexception-resteasy003145-unable-to-find-a-messagebodyreader-of-conte?noredirect=1&lq=1
        ClientInvocationBuilder builder = (ClientInvocationBuilder) client.register(StringTextStar.class)
                .target(uri)
                .queryParams(getConfigForQuery())
                .queryParam("to", Util.getCleanUserPhone(phone))
                .queryParam("text", URLEncoder.encode(text, smsConfig.getCharset()))
                .request();
        try {
            return builder.get(String.class);

        } catch (ProcessingException | WebApplicationException wae) {
            log.error(wae.getMessage(), wae);

            throw new SmsSendException(wae);
        }
    }

    private MultivaluedMap<String, Object> getConfigForQuery() {
        return new MultivaluedHashMap<>(Map.of(
                "smsc", smsConfig.getSmsCenterName(),
                "username", smsConfig.getUsername(),
                "password", smsConfig.getPassword(),
                "from", smsConfig.getSenderName(),
                "validity", smsConfig.getTimeout(),
                "priority", smsConfig.getPriority().getPriorityAsInt(),
                "dlr-mask", smsConfig.getReportsMask(),
                "coding", smsConfig.getEncoding().getPriorityAsInt(),
                "charset", smsConfig.getCharset()
        ));
    }
}
