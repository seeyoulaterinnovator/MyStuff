package ru.alamics.sso.keycloak.config.custom;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.NoCache;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.property.ApplicationProperties;

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
                .addResult("b2bChatWidgetServer", properties.getProperty("b2bChatWidget.server"))
                .build();
    }
}
