package ru.alamics.sso.keycloak.cities;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.cities.model.CityMigration;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.property.ApplicationProperties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class CitiesResource {
    private static final String CITIES_URL = "cities.url";
    private static String url;

    private static ReentrantLock lock = new ReentrantLock();

    private static List<CityMigration> cityList = new ArrayList<>();

    protected KeycloakSession session;

    public CitiesResource(KeycloakSession session) {
        this.session = session;
        ApplicationProperties properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);
        if (url == null) {
            url = properties.getProperty(CITIES_URL);
        }
    }

    @GET
    @Path("")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCities() {
        if (cityList.isEmpty()) {
            lock.lock();
            try {
                if (cityList.isEmpty()) {
                    cityList = SimpleHttp.doGet(url, session).asJson(new TypeReference<List<CityMigration>>() {
                    });
                }
            } catch (IOException e) {
                log.error("Connect to " + url + " failed");
            } finally {
                lock.unlock();
            }
        }
        return JsonResponse.success()
                .addResult("cities", cityList)
                .build();
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
}
