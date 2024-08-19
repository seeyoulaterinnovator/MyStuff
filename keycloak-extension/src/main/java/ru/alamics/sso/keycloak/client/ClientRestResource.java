package ru.alamics.sso.keycloak.client;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
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
        return new ClientResource(session, auth, auth.adminAuth(), findClientById(id));
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
