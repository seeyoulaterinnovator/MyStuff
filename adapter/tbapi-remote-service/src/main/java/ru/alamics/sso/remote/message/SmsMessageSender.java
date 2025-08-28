package ru.alamics.sso.remote.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.phone.exception.SendMessageException;
import ru.alamics.sso.registration.phone.model.MessageRequest;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.E2EUtil;
import ru.alamics.sso.util.StandResolver;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.net.URI;

import static ru.alamics.sso.settings.SettingConstants.*;

@ApplicationScoped
@Slf4j
public class SmsMessageSender implements Sender {
    private final static String MOCK_RESPONSE_STR = """
            {
              "requestId": "813cd8ec49548c764830bf72d16dad02"
            }
            """;
    @Inject
    SettingsService settingsService;
    @Inject
    ApplicationProperties properties;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpclient;

    public SmsMessageSender(
            @Named("smsSenderSSLContext") SSLContext sslContext,
            @Named("smsSenderHostnameVerifier") HostnameVerifier hostnameVerifier
    ) {
        httpclient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(hostnameVerifier)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(3_000)
                        .setConnectionRequestTimeout(3_000)
                        .setSocketTimeout(10_000)
                        .build())
                .build();
    }

    @Override
    public String send(MessageRequest request) throws SendMessageException {
        String type = request.getMessengerName().getType();
        String realmId = request.getRealmId();

        URI uri = URI.create(getSetting(type + SEND_URI.getKey(), realmId));
        String serviceName = getSetting(type + SENDER_NAME.getKey(), realmId);
        Boolean simulate = "1".equals(getSetting(type + SIMULATE.getKey(), realmId));
        String token = getSetting(type + AUTH_TOKEN.getKey(), realmId);

        String phone = request.getUserPhone();

        SmsRequest sms = new SmsRequest(phone, request.getText(), serviceName, simulate);
        String requestJson = "";
        try {
            requestJson = objectMapper.writeValueAsString(sms);
        } catch (JsonProcessingException e) {
            throw new SendMessageException("Cant convert to json: " + e.getMessage());
        }

        // локально и на дэве фиксированный код и не отправляю смс
        if (!StandResolver.isBattle() && !E2EUtil.isE2E() || isSmsSenderMocked()) {
            return getMockResponse(requestJson);
        }
        log.info("Api {}, Sending {} code to number: {}", uri.getHost(), request.getMessengerName().toString(), phone);

        HttpPost httpPost = new HttpPost(uri);
        HttpEntity entity = new StringEntity(requestJson, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        httpPost.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        String result;

        try {
            HttpResponse response = httpclient.execute(httpPost);
            try {
                var status = response.getStatusLine().getStatusCode();
                switch (status) {
                    case HttpStatus.SC_BAD_REQUEST:
                    case HttpStatus.SC_FORBIDDEN:
                        throw new SendMessageException(EntityUtils.toString(response.getEntity()));
                    case HttpStatus.SC_ACCEPTED:
                    case HttpStatus.SC_OK:
                        result = EntityUtils.toString(response.getEntity());
                        break;
                    default: throw new SendMessageException(response.getStatusLine().getReasonPhrase());
                }
            } finally {
                EntityUtils.consume(response.getEntity());
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            throw new SendMessageException(e.getMessage(), e);
        }

        return result;
    }

    private String getMockResponse(String requestJson) {
        log.info("Stand {}, do not sending sms. Request: {}", StandResolver.ENV, requestJson);

        return MOCK_RESPONSE_STR;
    }

    protected boolean isSmsSenderMocked() {
        return "true".equals(properties.getProperty("smsSender.mocked"));
    }

    protected String getSetting(String key, String realmId) {
        return settingsService.getSettingsStringValue(key, realmId);
    }
}
