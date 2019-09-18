package ru.alamics.sso.remote.tbapi;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import ru.alamics.sso.registration.tbapi.exception.TbapiRegisterException;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.registration.tbapi.model.TbapiRequest;
import ru.alamics.sso.registration.tbapi.port.TbapiRemoteService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TbapiServiceRestImpl implements TbapiRemoteService {

    private static final Map<String, Object> mapExample = Collections.unmodifiableMap(new HashMap<>());

    private static final ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS);

    private static final ResteasyClient client = clientBuilder.build();

    @Override
    public Map<String, Object> createCustomer(TbapiRequest request, TbapiConnectConfig connectConfig) throws TbapiRegisterException
    {

        URI uri = new ResteasyUriBuilder()
                .scheme(connectConfig.isSecure() ? "https" : "http")
                .host(connectConfig.getHost())
                .port(connectConfig.getPort())
                .path(connectConfig.getPath())
                .build();

        ResteasyWebTarget target = client.target(uri);
        target.request(MediaType.APPLICATION_JSON);

        Entity<TbapiRequest> entity = Entity.json(request);

        Map<String, Object> responseMap;
        try (Response response = target
                .register(ResteasyJackson2Provider.class) // TODO
                .request()
                .header("Accept", MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Trusted application=\"%s\", username=\"%s\"", connectConfig.getAppname(), connectConfig.getUsername()))
                .post(entity)) {

            responseMap = response.readEntity(new GenericType<>(mapExample.getClass()));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //responseMap = Map.of();
            throw new TbapiRegisterException(e);
        }

        return responseMap;
    }

    @Override
    public Map<String, Object> getCustomerName(TbapiConnectConfig connectConfig) {
        URI uri = new ResteasyUriBuilder()
                .scheme(connectConfig.isSecure() ? "https" : "http")
                .host(connectConfig.getHost())
                .port(connectConfig.getPort())
                .path(connectConfig.getPath())
                .build();

        ResteasyWebTarget target = client.target(uri);
        target.request(MediaType.APPLICATION_JSON);
        Response response = target.register(ResteasyJackson2Provider.class).request().get();
        Map<String, Object> responseMap = response.readEntity(new GenericType<>(mapExample.getClass()));
        return responseMap;
    }
}
