package ru.alamics.sso.keycloak.user.resource.session;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.NoCache;
import jakarta.ws.rs.QueryParam;
import org.jboss.resteasy.reactive.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;

import java.util.List;
import java.util.stream.Collectors;

public class CustomSessions {

    protected KeycloakSession session;

    private AdminPermissionEvaluator auth;

    private RealmModel realm;

    @Inject
    public CustomSessions(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.auth = auth;
    }

    @Path("/{id}")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserSessionRepresentation> getSessions(@PathParam("id") String id, @QueryParam("searchRealm") String realmName) {
        realm = session.realms().getRealmByName(realmName);
        UserModel user = session.users().getUserById(realm, id);
        auth.users().requireView(user);

        return session
                .sessions()
                .getUserSessionsStream(realm, user)
                .map(ModelToRepresentation::toRepresentation)
                .collect(Collectors.toList());
    }
}
