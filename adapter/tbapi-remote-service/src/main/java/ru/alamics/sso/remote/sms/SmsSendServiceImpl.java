package ru.alamics.sso.remote.sms;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import ru.alamics.sso.registration.phone.SmsConfig;
import ru.alamics.sso.registration.phone.port.SmsSendService;

import javax.ejb.Stateless;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Stateless(name = "SmsSender")
public class SmsSendServiceImpl implements SmsSendService {

    private static final String SMSC_NAME = "centerName";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "pass";
    private static final String SENDER_NAME = "sender";
    private final ResteasyClient client = new ResteasyClientBuilder().build();
    private SmsConfig smsConfig;

    public SmsSendServiceImpl() {
        smsConfig = SmsConfig.builder()
                .url(new ResteasyUriBuilder()
                        .scheme("http")
                        .host("smsgw.testing.ertelecom.ru")
                        .port(13003)
                        .path("cgi-bin/sendsms")
                        .build())
                .smsCenterName(SMSC_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .senderName(SENDER_NAME)
                .timeout(5)
                .priority(SmsConfig.Priority.HIGH)
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

        URI uri = smsConfig.getUrl();

        String response = client.target(uri)
                .queryParams(getConfigForQuery())
                .queryParam("to", phone)
                .queryParam("text", URLEncoder.encode(text, smsConfig.getCharset()))
                .request()
                .post(null, String.class);

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
