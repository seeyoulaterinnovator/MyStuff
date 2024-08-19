package ru.alamics.sso.remote.tbapi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import ru.alamics.sso.registration.tbapi.exception.TbapiRegisterException;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.registration.tbapi.model.TbapiRequest;
import ru.alamics.sso.registration.tbapi.model.TbapiResponse;
import ru.alamics.sso.registration.tbapi.port.TbapiRemoteService;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@Named("TbapiRemoteService")
@Slf4j
public class TbapiServiceRestImpl implements TbapiRemoteService {

    private static final Map<String, Object> mapExample = Collections.unmodifiableMap(new HashMap<>());

    private static final ResteasyClientBuilder clientBuilder = ((ResteasyClientBuilder) ClientBuilder.newBuilder())
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .disableTrustManager();

    private static final ResteasyClient client = clientBuilder.build();

    @Override
    public TbapiResponse createCustomer(TbapiRequest request, TbapiConnectConfig connectConfig) throws TbapiRegisterException {
        // нужно кидать exception для показа страницы с ошибкой
        if (request != null && request.getLegalName() != null && request.getLegalName().equalsIgnoreCase("ПВФ Сейлор Мун")) {
            log.info("Во имя луны!");
            throw new TbapiRegisterException();
        }

        return createCustomerBattle(request, connectConfig);
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

        ResteasyWebTarget target = client.target(uri);

        Entity<TbapiRequest> entity = Entity.json(request);

        TbapiResponse responseData = null;
        try (Response response = target
                .register(ResteasyJackson2Provider.class)
                .register(StringTextStar.class)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .header("HOST", connectConfig.getHost())
                .header("Authorization", String.format("Trusted application=\"%s\", username=\"%s\", password=\"%s\"",
                        connectConfig.getAppname(), connectConfig.getUsername(), connectConfig.getPassword()))
                .post(entity)) {

            log.info("response media type {}, status {}", response.getMediaType(), response.getStatus());

            if (response.getMediaType().toString().equalsIgnoreCase("text/html")) {
                log.error("response " + response.readEntity(String.class));
            } else {
                responseData = response.readEntity(TbapiResponse.class);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //responseMap = Map.of();
            throw new TbapiRegisterException(e);
        }
        finally {
            log.info("TbapiServiceRestImpl has ended");
        }

        return responseData;
    }

    @Override
    public Map<String, Object> getCustomerName(List<String> id, TbapiConnectConfig connectConfig) {
        log.info("customer names request : customerIds={}", id);
        Map<String, Object> responseMap = new HashMap<>();

        Response response = null;

        try {
            URI uri = UriBuilder.newInstance()
                    .scheme(connectConfig.isSecure() ? "https" : "http")
                    .host(connectConfig.getIp())
                    .port(connectConfig.getPort())
                    .path(connectConfig.getPath())
                    .build();

            log.info("TBAPI request to {}", uri.toString());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("id", id);
            Entity<Map<String, Object>> entity = Entity.json(requestBody);

            ResteasyWebTarget target = client.target(uri);
            response = target.register(ResteasyJackson2Provider.class).request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("HOST", connectConfig.getHost())
                    .header("Authorization", String.format("Trusted application=\"%s\", username=\"%s\", password=\"%s\"",
                            connectConfig.getAppname(), connectConfig.getUsername(), connectConfig.getPassword()))
                    .build("POST", entity)
                    .invoke();

            log.info("response media type {}, status {}", response.getMediaType(), response.getStatus());

            if (response.getMediaType().toString().equalsIgnoreCase("text/html")) {
                log.error("response " + response.readEntity(String.class));

            } else {

                responseMap = response.readEntity(new GenericType<>(mapExample.getClass()));
                if (responseMap.get("businessErrorCode") != null) {
                    throw new Exception("error tbapi code: " + responseMap.get("businessErrorCode").toString());
                }
            }

            log.info("customer names response : {}", responseMap);

        } catch (Exception e) {
            log.error("tbapi error post request: ", e);
        } finally {

            if (response != null)
                response.close();
        }
        return responseMap;
    }
}
