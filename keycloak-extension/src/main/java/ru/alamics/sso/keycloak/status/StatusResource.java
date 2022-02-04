package ru.alamics.sso.keycloak.status;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.status.StatusService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class StatusResource {

    private static AtomicLong healthUpdated = new AtomicLong(0);
    private static AtomicBoolean healthStatus = new AtomicBoolean(false);
    protected KeycloakSession session;
    private StatusService statusService;

    public StatusResource(KeycloakSession session) {
        this.session = session;
        this.statusService = Lookup.lookup(StatusService.class);
    }

    @GET
    @Path("health")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getStatus() {

        if (statusService == null) {

            return JsonResponse.error(Response.Status.NO_CONTENT)
                    .addResult("status", true)
                    .build();
        }

        long start = System.currentTimeMillis();
        boolean cached = false;
        boolean curStatus = false;

        try {
            if (start - healthUpdated.get() >= 500) {
                healthUpdated.set(start);
            } else {
                cached = true;
                curStatus = healthStatus.get();
                return buildResponse(curStatus);
            }

            healthStatus.set(statusService.checkStatusDb());
            curStatus = healthStatus.get();

            return buildResponse(curStatus);

        } finally {
            long complete = System.currentTimeMillis();
            log.info("Health check {}, {} ms {}", curStatus ? "OK" : "FAIL", (complete - start), cached ? "cached" : "");
        }
    }

    private Response buildResponse(boolean status) {

        if (!status) {
            return JsonResponse.error(Response.Status.SERVICE_UNAVAILABLE)
                    .addResult("status", status)
                    .build();
        }

        return JsonResponse.success()
                .addResult("status", status)
                .build();
    }
}
