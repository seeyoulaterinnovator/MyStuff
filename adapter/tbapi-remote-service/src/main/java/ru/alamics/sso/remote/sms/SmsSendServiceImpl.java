package ru.alamics.sso.remote.sms;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocationBuilder;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import ru.alamics.sso.registration.phone.SmsConfig;
import ru.alamics.sso.registration.phone.port.SmsSendService;
import ru.alamics.sso.util.EStand;
import ru.alamics.sso.util.StandResolver;

import javax.ejb.Stateless;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Stateless(name = "SmsSender")
public class SmsSendServiceImpl implements SmsSendService {

    private static final String SMSC_NAME = "rapporto_gold";
    private static final String USERNAME = "ertelecom";
    private static final String PASSWORD = "P10BxzA6Z1BRM";
    private static final String SENDER_NAME = "Domru";
    private final ResteasyClient client = new ResteasyClientBuilder().build();
    private SmsConfig smsConfig;

    public SmsSendServiceImpl() {
        smsConfig = SmsConfig.builder()
                .url(new ResteasyUriBuilder()
                        .scheme("http")
                        .host("smsgw.ertelecom.ru")
                        .port(13003)
                        .path("cgi-bin/sendsms")
                        .build())
                .smsCenterName(SMSC_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .senderName(SENDER_NAME)
                .timeout(1440)
                .priority(SmsConfig.Priority.LOWEST)
                .reportsMask(SmsConfig.ReportsConfig.DELIVERED_TO_PHONE)
                .encoding(SmsConfig.Encoding.UCS2)
                .charset(StandardCharsets.UTF_8)
                .build();
    }

    public SmsSendServiceImpl(SmsConfig smsConfig) {
        this.smsConfig = smsConfig;
    }

    @Override
    public String sendSms(String phone, String text) {

        // локально и на дэве фиксированный код и не отправляю смс
        if (!StandResolver.ENV.isBattle()) {
            log.info("Stand {}, do not sending sms", StandResolver.ENV);
            return "0: Accepted for delivery";
        }

        phone = phone.replaceAll("[^0-9]+", "");

        URI uri = smsConfig.getUrl();

        // TODO https://stackoverflow.com/questions/53760939/processingexception-resteasy003145-unable-to-find-a-messagebodyreader-of-conte?noredirect=1&lq=1
        ClientInvocationBuilder builder = (ClientInvocationBuilder)client.register(StringTextStar.class)
                .target(uri)
                .queryParams(getConfigForQuery())
                .queryParam("to", phone)
                .queryParam("text", URLEncoder.encode(text, smsConfig.getCharset()))
                .request()
                ;

        String response = builder.get(String.class);

        return response;
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
