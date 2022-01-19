package ru.alamics.sso.keycloak.user.resource.session;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

public class CustomSessions {

    protected KeycloakSession session;

    private AdminPermissionEvaluator auth;

    private RealmModel realm;

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
        UserModel user = session.users().getUserById(id, realm);
        auth.users().requireView(user);

        return session
                .sessions()
                .getUserSessions(realm, user)
                .stream()
                .map(ModelToRepresentation::toRepresentation)
                .collect(Collectors.toList());
    }
}
