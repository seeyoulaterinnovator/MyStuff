package ru.alamics.sso.keycloak.cities;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JsonSerialization;
import ru.alamics.sso.keycloak.cities.model.CityMigration;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.property.ApplicationProperties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class CitiesResource {

    private static final ResteasyClient client = new ResteasyClientBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .disableTrustManager()
            .build();
    private static final String CITIES_URL = "cities.url";
    private static final long CACHE_TIME = 60 * 60 * 1000; // 1h
    private static final ReentrantLock lock = new ReentrantLock();
    private static final AtomicLong updated = new AtomicLong(0);

    private static final String TOKEN = "af99ee7d5076c5d5efd38fc213761d69452e11c4";
    private static final String URL = "https://suggestions.dadata.ru/suggestions/api/4_1/rs/iplocate/address";
    private static final String LANGUAGE = "ru";

    private static String url;
    private static List<CityMigration> cityList = new ArrayList<>();

    protected KeycloakSession session;

    public CitiesResource(KeycloakSession session) {
        this.session = session;
        ApplicationProperties properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);
        if (url == null && properties != null) {
            url = properties.getProperty(CITIES_URL);
        }
    }

    public static List<CityMigration> getCityList() {
        return cityList;
    }

    public static CityMigration getCityMigrationByCity(String city) {

        if (city == null)
            return null;

        for (CityMigration cm : cityList) {
            if (city.equalsIgnoreCase(cm.getCity()))
                return cm;
        }

        return null;
    }

    public static CityMigration getCityMigrationByDomain(String domain) {

        if (domain == null)
            return null;

        for (CityMigration cm : cityList) {
            if (domain.equalsIgnoreCase(cm.getDomain()))
                return cm;
        }

        return null;
    }

    @GET
    @Path("")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCities() {

        long now = System.currentTimeMillis();

        if (cityList.isEmpty() || now > updated.get() + CACHE_TIME) {
            lock.lock();
            try {
                if (cityList.isEmpty() || now > updated.get() + CACHE_TIME) {
                    try (Response response = client.target(url)
                            .register(ResteasyJackson2Provider.class)
                            .register(StringTextStar.class)
                            .request()
                            .accept(MediaType.APPLICATION_JSON)
                            .get()) {
                        log.info("response media type {}, status {}", response.getMediaType(), response.getStatus());
                        if (response.getMediaType().toString().equalsIgnoreCase("text/html")) {
                            log.error("response " + response.readEntity(String.class));
                        } else {
                            cityList = response.readEntity(new GenericType<List<CityMigration>>() {
                            });
                        }
                    }
                    updated.set(now);
                }
            } finally {
                lock.unlock();
            }
        }
        return JsonResponse.success()
                .addResult("cities", cityList)
                .build();
    }

    @GET
    @Path("/title")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCityTitle() {
        String ipAddress = session.getContext().getConnection().getRemoteAddr();

        ResteasyWebTarget wt = client.target(URL)
                .queryParam("ip", ipAddress)
                .queryParam("languege", LANGUAGE);

        String json = wt.request(MediaType.APPLICATION_JSON)
                .header("Accept", "application/json")
                .header("Authorization", "TOKEN " + TOKEN)
                .get(String.class);

        String title = "";

        try {
            Map jsonObject = JsonSerialization.readValue(json, Map.class);
            ObjectNode objectNode = JsonSerialization.createObjectNode(jsonObject);
            title = objectNode.findValue("value").textValue();
            title = title.substring(2);
        } catch (Exception err) {

        }

        return JsonResponse.success()
                .addResult("title", title)
                .build();

    }
}
