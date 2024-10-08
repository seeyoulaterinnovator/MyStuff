package ru.alamics.sso.keycloak.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.NoCache;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.client.ClientService;
import ru.alamics.sso.keycloak.lookup.Lookup;

@Slf4j
public class ClientResource extends org.keycloak.services.resources.admin.ClientResource {
    private final ClientService service;

    private final ObjectMapper objectMapper;

    public ClientResource(
            RealmModel realm,
            AdminPermissionEvaluator auth,
            ClientModel clientModel,
            KeycloakSession session,
            AdminEventBuilder adminEvent
    ) {
        super(realm, auth, clientModel, session, adminEvent);
        this.service = Lookup.lookup(ClientService.class);
        this.objectMapper = Lookup.lookup(ObjectMapper.class);
    }

    @Override
    public ClientRepresentation getClient() {
        throw new NotFoundException();
    }

    @Override
    public Response update(ClientRepresentation rep) {
        throw new NotFoundException();
    }

    @GET
    @Path("/ext")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public ObjectNode getCustomClient() {
        var client = super.getClient();
        ObjectNode customClient = objectMapper.valueToTree(client);
        customClient.set("mainRedirectUri", new TextNode(service.getMainRedirectUri(client.getId())));
        return customClient;
    }

    @PUT
    @Path("/ext")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCustom(final CustomClientRepresentation rep) {
        try {
            service.saveMainRedirectUri(client.getId(), rep.getMainRedirectUri());
            return super.update(rep);
        } catch (ModelDuplicateException e) {
            return ErrorResponse.exists("Client " + rep.getClientId() + " already exists").getResponse();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CustomClientRepresentation extends ClientRepresentation {
        private String mainRedirectUri;
    }
}
