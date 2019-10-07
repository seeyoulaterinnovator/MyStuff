package ru.alamics.sso.remote.viber;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import ru.alamics.sso.registration.phone.SmsConfig;
import ru.alamics.sso.registration.phone.exception.ViberSendException;
import ru.alamics.sso.registration.phone.port.ViberSendService;
import ru.alamics.sso.util.EStand;
import ru.alamics.sso.util.StandResolver;
import ru.alamics.sso.util.Util;

import javax.ejb.Stateless;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Stateless(name = "ViberSender")
public class ViberSendServiceImpl implements ViberSendService {

    private static final String SMSC_NAME = "rapporto_viber";
    private static final String USERNAME = "ertelecom";
    private static final String PASSWORD = "P10BxzA6Z1BRM";
    private static final String SENDER_NAME = "Domru";

    private static final ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS);

    private static final ResteasyClient client = clientBuilder.build();
    private static SmsConfig smsConfig;

    public ViberSendServiceImpl() {
        smsConfig = SmsConfig.builder()
                .url(new ResteasyUriBuilder()
                        .scheme("http")
                        .host("smsgw-prior.ertelecom.ru")
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

    public ViberSendServiceImpl(SmsConfig config) {
        smsConfig = config;
    }

    @Override
    public String sendMsg(String phone, String text) throws ViberSendException
    {

        // локально и на дэве фиксированный код и не отправляю смс
        if (!StandResolver.isBattle()) {
            log.info("Stand {}, do not sending viber msg", StandResolver.ENV);
            return "0: Accepted for delivery";
        }

        URI uri = smsConfig.getUrl();

        try {
            ResteasyWebTarget webTarget = client.target(uri)
                    .queryParams(getConfigForQuery())
                    .queryParam("to", Util.getCleanUserPhone(phone))
                    .queryParam("text", URLEncoder.encode(text, smsConfig.getCharset()));

            //System.out.println(webTarget.getUri());

            String response = webTarget
                    .request()
                    .get(String.class);

            //System.out.println(response);

            return response;

        } catch (ProcessingException | WebApplicationException wae) {
            log.error(wae.getMessage(), wae);

            throw new ViberSendException(wae);
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
