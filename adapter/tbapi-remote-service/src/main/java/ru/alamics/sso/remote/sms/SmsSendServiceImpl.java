package ru.alamics.sso.remote.sms;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import ru.alamics.sso.registration.phone.SmsConfig;
import ru.alamics.sso.registration.phone.port.SmsSendService;

import javax.ejb.Stateless;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Stateless
public class SmsSendServiceImpl implements SmsSendService {

    private final ResteasyClient client = new ResteasyClientBuilder().build();
    private final String scheme = "https";
    private final String host = "";
    private final int port = 80;
    private final String path = "";
    private final SmsConfig smsConfig = SmsConfig.builder().build();

    public SmsSendServiceImpl() {

    }

    @Override
    public Integer sensSms(String phone, String text) {

        String path = "?smsc=" + smsConfig.getSmsCenterName() +
                "&username=" + smsConfig.getUsername() +
                "&password=" + smsConfig.getPassword() +
                "&to=" + phone +
                "&from=" + smsConfig.getSenderName() +
                "&validity=" + smsConfig.getTimeout() +
                "&priority=" + smsConfig.getPriority().getPriorityAsInt() +
                "&dlr-mask=" + smsConfig.getReportsMask() +
                "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8) +
                "&coding=" + smsConfig.getEncoding().getPriorityAsInt() +
                "&charset=" + smsConfig.getCharset();

        URI uri = new ResteasyUriBuilder()
                .scheme(scheme)
                .host(host)
                .port(port)
                .path(this.path + path)
                .build();

        Response response = client.target(uri).request().post(null);

        String responseString = response.readEntity(String.class);

        int i;
        try {
            i = Integer.parseInt(responseString);
        } catch (NumberFormatException e) {
            log.warn(e.getMessage(), e);
            i = -1;
        }

        return i;
    }
}
