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
import org.apache.http.impl.client.HttpClients;
import org.jboss.resteasy.reactive.NoCache;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.cities.model.CityDadataModel;
import ru.alamics.sso.keycloak.cities.model.CityMigration;
import ru.alamics.sso.keycloak.cities.model.RegionCities;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.StandResolver;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class CitiesResource {
    private static final int CONNECT_TIMEOUT = 3_000;

    private static final int SOCKET_TIMEOUT = 10_000;

    private static final long CACHE_TIME = 60 * 60 * 1000; // 1h

    private static final String CITIES_URL = "cities.url";

    private static final String LANGUAGE_RU = "ru";

    private static final ReentrantLock LOCK = new ReentrantLock();

    private static final AtomicLong UPDATED = new AtomicLong(0);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final AtomicReference<List<CityMigration>> CITY_LIST_REF = new AtomicReference<>(new ArrayList<>());

    public static CityMigration getCityMigrationByCity(String city) {

        if (city == null)
            return null;

        for (CityMigration cm : CITY_LIST_REF.get()) {
            if (city.equalsIgnoreCase(cm.getCity()))
                return cm;
        }

        return null;
    }

    public static CityMigration getCityMigrationByName(String city) {

        if (city == null)
            return null;

        for (CityMigration cm : CITY_LIST_REF.get()) {
            if (city.equalsIgnoreCase(cm.getName()))
                return cm;
        }

        return null;
    }

    private final KeycloakSession session;

    private final SettingsService settingsService;

    private final HttpClient citiesClient;

    private final HttpClient dadataClient;

    public CitiesResource(KeycloakSession session) {
        this.session = session;
        settingsService = Lookup.lookup(SettingsService.class);
        citiesClient = HttpClients.custom()
                .setSSLContext(Lookup.lookup(SSLContext.class, "citiesSSLContext"))
                .setSSLHostnameVerifier(Lookup.lookup(HostnameVerifier.class, "citiesHostnameVerifier"))
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(CONNECT_TIMEOUT)
                        .setSocketTimeout(SOCKET_TIMEOUT)
                        .build())
                .build();
        dadataClient = HttpClients.custom()
                .setSSLContext(Lookup.lookup(SSLContext.class, "dadataSSLContext"))
                .setSSLHostnameVerifier(Lookup.lookup(HostnameVerifier.class, "dadataHostnameVerifier"))
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(CONNECT_TIMEOUT)
                        .setSocketTimeout(SOCKET_TIMEOUT)
                        .build())
                .build();
    }

    public void updateIfNeed() {
        if (CITY_LIST_REF.get().isEmpty() || System.currentTimeMillis() > UPDATED.get() + CACHE_TIME) {
            LOCK.lock();
            try {
                if (CITY_LIST_REF.get().isEmpty() || System.currentTimeMillis() > UPDATED.get() + CACHE_TIME) {
                    try {
                        HttpGet request = new HttpGet(Lookup.lookup(ApplicationProperties.class).getProperty(CITIES_URL));
                        request.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
                        try (InputStream stream = citiesClient.execute(request).getEntity().getContent()) {
                            CITY_LIST_REF.set(List.of(MAPPER.readValue(stream, CityMigration[].class)));
                        }
                        UPDATED.set(System.currentTimeMillis());
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            } finally {
                LOCK.unlock();
            }
        }
    }

    @GET
    @Path("")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCities() {
        updateIfNeed();
        return JsonResponse.success()
                .addResult("cities", CITY_LIST_REF.get())
                .build();
    }

    @GET
    @Path("/current")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCityTitle() throws Exception {
        updateIfNeed();
        String ipAddress = session.getContext().getConnection().getRemoteAddr();
        String url = settingsService.getSettingsStringValue(SettingConstants.URL_DADATA_REQUEST_LOCATION_IP, Config.getAdminRealm());
        String token = settingsService.getSettingsStringValue(SettingConstants.TOKEN_DADATA, Config.getAdminRealm());

        if (StandResolver.isBattle()) {
            getCities();
        }

        log.debug(String.format("Sending request with address %s to dadata", ipAddress));

        HttpGet request = new HttpGet(UriBuilder.fromUri(url)
                .queryParam("ip", ipAddress)
                .queryParam("language", LANGUAGE_RU)
                .build());
        request.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        request.setHeader(HttpHeaders.AUTHORIZATION, "TOKEN " + token);

        CityDadataModel cityDadataModel;
        try(InputStream stream = dadataClient.execute(request).getEntity().getContent()) {
            cityDadataModel = MAPPER.readValue(stream, CityDadataModel.class);
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
        log.debug("getCityTitle ended");

        return JsonResponse.success()
                .addResult("title", title)
                .build();

    }
}
