package ru.alamics.sso.keycloak.rest.messenger;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.service.MessengerService;

@Slf4j
public class MessengerResource {

    private final KeycloakSession session;
    private final AdminPermissionEvaluator auth;
    private final MessengerService messengerService;

    public MessengerResource(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.auth = auth;
        this.messengerService = Lookup.lookup(MessengerService.class);
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getAll() {
        return JsonResponse.success()
                .addResult("messengers", messengerService.getAllMessengers())
                .build();
    }
}
