package ru.alamics.sso.keycloak.config.custom;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.NoCache;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.GeneralRealm;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.property.ApplicationProperties;

@Slf4j
public class CustomConfigResource {
    final KeycloakSession session;

    final ApplicationProperties properties;

    public CustomConfigResource(KeycloakSession session) {
        this.session = session;
        properties = Lookup.lookup(ApplicationProperties.class);
    }

    @GET
    @Path("")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserPublicConfig() {
        return JsonResponse.success()
                .addResult("b2bChatWidgetUrl", properties.getProperty("b2bChatWidget.url"))
                .addResult("b2bChatWidgetServer", properties.getProperty("b2bChatWidget.server"))
                .build();
    }

    @GET
    @Path("/admin")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAdminPublicConfig() {
        return JsonResponse.success()
                .addResult("adminTheme", session.getContext().getRealm().getAdminTheme() == null ? "keycloak.v2" : session.getContext().getRealm().getAdminTheme())
                .addResult("manageRealmName", GeneralRealm.MANAGER_REALMS.get(0))
                .addResult("adminAllowedOrigins", properties.getPropertyList("admin.allowedOrigins"))
                .build();
    }
}
