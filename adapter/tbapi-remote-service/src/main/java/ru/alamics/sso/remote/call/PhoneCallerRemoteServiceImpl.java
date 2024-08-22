package ru.alamics.sso.remote.call;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.phone.exception.PhoneCallException;
import ru.alamics.sso.registration.phone.port.PhoneCallerRemoteService;
import ru.alamics.sso.util.Util;

import java.io.InputStream;
import java.net.URI;

@ApplicationScoped
@Named("PhoneCallerService")
@Slf4j
public class PhoneCallerRemoteServiceImpl implements PhoneCallerRemoteService {

    private static final String URI_PERM = "phoneCaller.uri.perm";
    private static final String URI_VORONEZH = "phoneCaller.uri.voronezh";

    private static final HttpClient client;
    static {
        try {
            client = HttpClients.custom()
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setConnectTimeout(3_000)
                            .setSocketTimeout(10_000)
                            .build())
                    .build();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Inject
    ApplicationProperties properties;

    private URI uriPerm;
    private URI uriVoronezh;

    @PostConstruct
    private void init() {
        try {
            uriPerm = URI.create(properties.getProperty(URI_PERM));
        } catch (Exception e) {
            log.error("Fail uriPerm initialize", e);
        }
        try {
            uriVoronezh = URI.create(properties.getProperty(URI_VORONEZH));
        } catch (Exception e) {
            log.error("Fail uriVoronezh initialize", e);
        }
    }

    @Override
    public String callAndGetCode(String phone, int count) throws PhoneCallException {

        String response = null;
        try {
            if (uriVoronezh != null) {
                response = getCode(uriVoronezh, Util.getCleanUserPhone(phone), count);
            }
            if (isNull(response)) {
                response = getCode(uriPerm, Util.getCleanUserPhone(phone), count);
            }
        } catch (BadRequestException e) {
            log.error("Error", e);
            if (HttpStatus.SC_BAD_REQUEST == e.getResponse().getStatus()) {
                throw new PhoneCallException("Incorrect phone number", e);
            }
        } catch (ProcessingException | WebApplicationException wae) {
            log.error("Error", wae);
            throw new PhoneCallException("Ошибка при выполнении звонка", wae);
        }

        if (isNull(response)) {
            log.error("Empty response");
            throw new PhoneCallException("Невозможно выполнить звонок");
        }
        return response;
    }

    private String getCode(URI uri, String phone, int count) throws PhoneCallException {
        log.info(String.format("Api %s, call to number: %s, count: %d", uri.getHost(), phone, count));

        HttpGet request = new HttpGet(UriBuilder.fromUri(uri)
                .queryParam("num", phone)
                .queryParam("retry", count)
                .build());
        request.setHeader(HttpHeaders.ACCEPT, MediaType.WILDCARD);
        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                try(InputStream stream = response.getEntity().getContent()) {
                    String code = new String(stream.readAllBytes());
                    code = code.replaceAll("\\n", "");
                    log.info(String.format("Api %s, code %s", uri.getHost(), code));
                    return code;
                }
            }
            throw new PhoneCallException(response.getStatusLine().getReasonPhrase());
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            throw new PhoneCallException(e.getMessage(), e);
        }
    }

    private boolean isNull(String field) {
        return field == null || field.isEmpty();
    }
}
