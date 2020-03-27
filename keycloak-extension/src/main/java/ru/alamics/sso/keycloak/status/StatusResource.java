package ru.alamics.sso.keycloak.status;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.property.ApplicationProperties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Slf4j
public class StatusResource {

    protected KeycloakSession session;
    private ApplicationProperties properties;

    public StatusResource(KeycloakSession session) {
        this.session = session;
        properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);
    }

    @GET
    @Path("health")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getStatus() {
        boolean status = properties.checkStatusDb();

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
