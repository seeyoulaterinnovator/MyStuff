package ru.alamics.sso.keycloak.config.custom;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.property.ApplicationProperties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Slf4j
public class CustomConfigResource {
    ApplicationProperties properties;

    public CustomConfigResource() {
        properties = Lookup.lookup(ApplicationProperties.class);
    }

    @GET
    @Path("")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCities() {
        return JsonResponse.success()
                .addResult("b2bChatWidgetUrl", properties.getProperty("b2bChatWidget.url"))
                .build();
    }
}
