package ru.alamics.sso.remote.tbapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import ru.alamics.sso.registration.tbapi.exception.TbapiRegisterException;
import ru.alamics.sso.registration.tbapi.model.TbapiConnect;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.registration.tbapi.model.TbapiRequest;
import ru.alamics.sso.registration.tbapi.model.TbapiResponse;
import ru.alamics.sso.registration.tbapi.port.TbapiRemoteService;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Named("TbapiRemoteService")
@Slf4j
public class TbapiServiceRestImpl implements TbapiRemoteService {
    private static final int CONNECT_TIMEOUT = 3_000;

    private static final int SOCKET_TIMEOUT = 10_000;

    private final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient tbapiRegistrationClient;

    private final HttpClient tbapiCustomerClient;

    public TbapiServiceRestImpl(
            @Named("tbapiRegistrationSSLContext") SSLContext tbapiRegistrationSslContext,
            @Named("tbapiCustomerSSLContext") SSLContext tbapiCustomerSslContext,
            @Named("tbapiRegistrationHostnameVerifier") HostnameVerifier tbapiRegistrationHostnameVerifier,
            @Named("tbapiCustomerHostnameVerifier") HostnameVerifier tbapiCustomerHostnameVerifier
    ) {
        tbapiRegistrationClient = HttpClients.custom()
                .setSSLContext(tbapiRegistrationSslContext)
                .setSSLHostnameVerifier(tbapiRegistrationHostnameVerifier)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(CONNECT_TIMEOUT)
                        .setSocketTimeout(SOCKET_TIMEOUT)
                        .build())
                .build();
        tbapiCustomerClient = HttpClients.custom()
                .setSSLContext(tbapiCustomerSslContext)
                .setSSLHostnameVerifier(tbapiCustomerHostnameVerifier)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(CONNECT_TIMEOUT)
                        .setSocketTimeout(SOCKET_TIMEOUT)
                        .build())
                .build();
    }

    @Override
    public TbapiResponse createCustomer(TbapiRequest request, TbapiConnectConfig connectConfig) throws TbapiRegisterException {
        // нужно кидать exception для показа страницы с ошибкой
        if (request != null && request.getLegalName() != null && request.getLegalName().equalsIgnoreCase("ПВФ Сейлор Мун")) {
            log.info("Во имя луны!");
            throw new TbapiRegisterException();
        }

        return createCustomerBattle(request, connectConfig);
    }

    private HttpClient getClient(TbapiConnectConfig connectConfig) {
        if(connectConfig.getConnect() == TbapiConnect.CUTOMER_NAMES) {
            return tbapiCustomerClient;
        } else {
            return tbapiRegistrationClient;
        }
    }

    private TbapiResponse createCustomerBattle(TbapiRequest request, TbapiConnectConfig connectConfig) throws TbapiRegisterException {
        log.info("TbapiServiceRestImpl");

        URI uri = UriBuilder.newInstance()
                .scheme(connectConfig.isSecure() ? "https" : "http")
                .host(connectConfig.getIp())
                .port(connectConfig.getPort())
                .path(connectConfig.getPath())
                .build();

        log.info("TBAPI request to {}", uri.toString());
        log.info("TBAPI config {}", connectConfig);

        try {
            HttpPost httpRequest = new HttpPost(uri);
            httpRequest.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            httpRequest.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            httpRequest.setHeader(HttpHeaders.HOST, connectConfig.getHost());
            httpRequest.setHeader(
                    HttpHeaders.AUTHORIZATION,
                    String.format("Trusted application=\"%s\", username=\"%s\", password=\"%s\"",
                            connectConfig.getAppname(), connectConfig.getUsername(), connectConfig.getPassword())
            );
            httpRequest.setEntity(new StringEntity(mapper.writeValueAsString(request)));
            HttpResponse httpResponse = getClient(connectConfig).execute(httpRequest);
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Header contentType = httpResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE);
                if(contentType != null && MediaType.TEXT_HTML.equals(contentType.getValue())) {
                    try(InputStream stream = httpResponse.getEntity().getContent()) {
                        log.error("response " + new String(stream.readAllBytes()));
                    }
                    throw new TbapiRegisterException();
                } else {
                    try(InputStream stream = httpResponse.getEntity().getContent()) {
                        return mapper.readValue(stream, TbapiResponse.class);
                    }
                }
            } else {
                throw new TbapiRegisterException(httpResponse.getStatusLine().getReasonPhrase());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new TbapiRegisterException(e);
        } finally {
            log.info("TbapiServiceRestImpl has ended");
        }
    }

    @Override
    public Map<String, Object> getCustomerName(List<String> id, TbapiConnectConfig connectConfig) throws TbapiRegisterException {
        log.info("customer names request : customerIds={}", id);

        try {
            URI uri = UriBuilder.newInstance()
                    .scheme(connectConfig.isSecure() ? "https" : "http")
                    .host(connectConfig.getIp())
                    .port(connectConfig.getPort())
                    .path(connectConfig.getPath())
                    .build();

            log.info("TBAPI request to {}", uri.toString());

            Map<String, Object> request = Map.of("id", id);

            try {
                HttpPost httpRequest = new HttpPost(uri);
                httpRequest.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                httpRequest.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
                httpRequest.setHeader(HttpHeaders.HOST, connectConfig.getHost());
                httpRequest.setHeader(
                        HttpHeaders.AUTHORIZATION,
                        String.format("Trusted application=\"%s\", username=\"%s\", password=\"%s\"",
                                connectConfig.getAppname(), connectConfig.getUsername(), connectConfig.getPassword())
                );
                httpRequest.setEntity(new StringEntity(mapper.writeValueAsString(request)));
                HttpResponse httpResponse = getClient(connectConfig).execute(httpRequest);
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    Header contentType = httpResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE);
                    if(contentType != null && MediaType.TEXT_HTML.equals(contentType.getValue())) {
                        try(InputStream stream = httpResponse.getEntity().getContent()) {
                            log.error("response " + new String(stream.readAllBytes()));
                        }
                        throw new TbapiRegisterException();
                    } else {
                        Map<String, Object> response;
                        try(InputStream stream = httpResponse.getEntity().getContent()) {
                            response = mapper.readValue(stream, new TypeReference<>() {});
                        }
                        if (response.get("businessErrorCode") != null) {
                            throw new Exception("error tbapi code: " + response.get("businessErrorCode").toString());
                        }
                        log.info("customer names response : {}", response);
                        return response;
                    }
                } else {
                    throw new TbapiRegisterException(httpResponse.getStatusLine().getReasonPhrase());
                }
            } catch (Exception e) {
                throw new TbapiRegisterException(e);
            }

        } catch (Exception e) {
            log.error("tbapi error post request: ", e);
            throw new TbapiRegisterException(e);
        }
    }
}
