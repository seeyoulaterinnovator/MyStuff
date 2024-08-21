package ru.alamics.sso.keycloak.cities;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.jboss.resteasy.reactive.NoCache;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.GeneralRealm;
import ru.alamics.sso.keycloak.cities.model.CityDadataModel;
import ru.alamics.sso.keycloak.cities.model.CityMigration;
import ru.alamics.sso.keycloak.cities.model.RegionCities;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.StandResolver;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class CitiesResource {
    private static final HttpClient client;
    static {
        try {
            client = HttpClients.custom()
                    .setSSLContext(new SSLContextBuilder()
                            .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                            .build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setConnectTimeout(3_000)
                            .setSocketTimeout(10_000)
                            .build())
                    .build();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String CITIES_URL = "cities.url";
    private static final long CACHE_TIME = 60 * 60 * 1000; // 1h
    private static final ReentrantLock lock = new ReentrantLock();
    private static final AtomicLong updated = new AtomicLong(0);

    private static final String LANGUAGE_RU = "ru";

    private static String url;
    private static List<CityMigration> cityList = new ArrayList<>();
    private final SettingsService settingsService;
    protected KeycloakSession session;

    public CitiesResource(KeycloakSession session) {
        this.session = session;
        ApplicationProperties properties = Lookup.lookup(ApplicationProperties.class);

        this.settingsService = Lookup.lookup(SettingsService.class);

        if (url == null && properties != null) {
            url = properties.getProperty(CITIES_URL);
        }
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

    public static CityMigration getCityMigrationByName(String city) {

        if (city == null)
            return null;

        for (CityMigration cm : cityList) {
            if (city.equalsIgnoreCase(cm.getName()))
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
                    HttpGet request = new HttpGet(url);
                    request.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
                    try (InputStream stream = client.execute(new HttpGet(url)).getEntity().getContent()) {
                        cityList = List.of(mapper.readValue(stream, CityMigration[].class));
                    }
                    updated.set(now);
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            } finally {
                lock.unlock();
            }
        }
        return JsonResponse.success()
                .addResult("cities", cityList)
                .build();
    }

    @GET
    @Path("/current")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCityTitle() throws Exception {
        String ipAddress = session.getContext().getConnection().getRemoteAddr();
        String url = settingsService.getSettingsStringValue(SettingConstants.URL_DADATA_REQUEST_LOCATION_IP, GeneralRealm.MASTER);
        String token = settingsService.getSettingsStringValue(SettingConstants.TOKEN_DADATA, GeneralRealm.MASTER);

        if (StandResolver.isBattle()) {
            getCities();
        }

        log.info(String.format("Sending request with address %s to dadata", ipAddress));

        HttpGet request = new HttpGet(UriBuilder.fromUri(url)
                .queryParam("ip", ipAddress)
                .queryParam("language", LANGUAGE_RU)
                .build());
        request.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        request.setHeader(HttpHeaders.AUTHORIZATION, "TOKEN " + token);

        CityDadataModel cityDadataModel;
        try(InputStream stream = client.execute(request).getEntity().getContent()) {
            cityDadataModel = mapper.readValue(stream, CityDadataModel.class);
        }

        String title = null;
        if (cityDadataModel != null) {
            String regionIsoCode = null;
            if (cityDadataModel.getLocation() != null) {
                if (cityDadataModel.getLocation().getData() != null) {
                    title = cityDadataModel.getLocation().getData().getCity();
                    regionIsoCode = cityDadataModel.getLocation().getData().getRegionIsoCode();
                }
            }
            CityMigration city = getCityMigrationByName(title);

            if (city == null && regionIsoCode != null) {
                RegionCities region = RegionCities.findRegionByIsoCode(regionIsoCode);
                city = region == null ? null : getCityMigrationByName(region.getDefaultCity());
            }

            title = city == null ? null : city.getName();
        }
        log.info("getCityTitle ended");

        return JsonResponse.success()
                .addResult("title", title)
                .build();

    }
}
