package ru.alamics.sso.keycloak.cities;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.response.JsonResponse;

import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class CitiesResource {

    private final static String url = "https://master.lk-backend.b2bweb.t2.ertelecom.ru/site/domains";
    //private final static String url = "https://api-lkb2b.domru.ru/site/domains";

    private static ReentrantLock lock = new ReentrantLock();

    private static JsonNode cities;

    protected KeycloakSession session;

    public CitiesResource(KeycloakSession session) {
        this.session = session;
    }

    @GET
    @Path("")
    @NoCache
    public Response getCities() {
        if (cities == null) {
            lock.lock();
            try {
                cities = SimpleHttp.doGet(url, session).asJson();
            }  catch (IOException e){
                log.error("Connect to " + url + " failed");
            } finally {
                lock.unlock();
            }
        }
        return  JsonResponse.success()
                .addResult("cities", cities)
                .build();
    }
}
