package ru.alamics.sso.keycloak.client;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;

public class ClientRestResource {

    private KeycloakSession session;
    private AdminPermissionEvaluator auth;

    public ClientRestResource(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.auth = auth;
    }

    @Path("{id}")
    public ClientResource getSearchResource(final @PathParam("id") String id) {
        RealmModel realm = session.getContext().getRealm();
        AdminEventBuilder adminEvent = new AdminEventBuilder(realm, auth.adminAuth(), session, session.getContext().getConnection())
                .realm(realm).resource(ResourceType.REALM);
        return new ClientResource(realm, auth, findClientById(id), session, adminEvent);
    }

    private ClientModel findClientById(String id) {
        ClientModel clientModel = session.getContext().getRealm().getClientById(id);
        if (clientModel == null) {
            // we do this to make sure somebody can't phish ids
            if (auth.clients().canList()) throw new NotFoundException("Could not find client");
            else throw new ForbiddenException();
        }

        session.getContext().setClient(clientModel);

        return clientModel;
    }
}
