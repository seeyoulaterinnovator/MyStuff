package ru.alamics.sso.remote.tbapi;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import ru.alamics.sso.registration.model.TbapiRequest;
import ru.alamics.sso.registration.port.TbapiRemoteService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TbapiServiceRestImpl implements TbapiRemoteService {

    private static final Map<String, Object> mapExample = Collections.unmodifiableMap(new HashMap<>());
    private final ResteasyClient client = new ResteasyClientBuilder().build();


    @Override
    public Map<String, Object> createLead(TbapiRequest request, String host, int port, String path, boolean secure) {

        URI uri = new ResteasyUriBuilder()
                .scheme(secure ? "https" : "http")
                .host(host)
                .port(port)
                .path(path)
                .build();

        ResteasyWebTarget target = client.target(uri);
        target.request(MediaType.APPLICATION_JSON);

        Entity<TbapiRequest> entity = Entity.json(request);

        Response response = target
                .request()
                .header("Accept", MediaType.APPLICATION_JSON)
                .post(entity);

        Map<String, Object> responseMap = response.readEntity(new GenericType<>(mapExample.getClass()));

        response.close();
        return responseMap;
    }}
