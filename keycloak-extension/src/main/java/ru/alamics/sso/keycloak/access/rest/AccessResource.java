package ru.alamics.sso.keycloak.access.rest;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.dto.UserPostDto;
import ru.alamics.sso.registration.service.UserPostService;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Slf4j
public class AccessResource {


    protected KeycloakSession session;
    private UserPostService accessService = new UserPostService();

    public AccessResource(KeycloakSession session) { this.session = session;    }

    private EntityManager getEM() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response create(UserPostDto userPostDto, HttpHeaders headers) {
        //authenticateRealmAdminRequest(new RealmManager(session).getRealmByName("master"));
        return JsonResponse.success()
                .addResult("access", accessService.save(userPostDto))
                .build();
    }

    /*
    @POST
    @Path("/edit")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response edit(Access access, HttpHeaders headers) {
        authenticateRealmAdminRequest(new RealmManager(session).getRealmByName("master"));
        return JsonResponse.success()
                .addResult("access", accessService.save(access))
                .build();
    }

    @POST
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response delete(@PathParam("id") String id) {
        authenticateRealmAdminRequest(new RealmManager(session).getRealmByName("master"));
        return JsonResponse.success()
                .build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response get(@PathParam("id") String id) {
        return JsonResponse.success()
                .addResult("users-info", null)
                .build();
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getAll() {
        return JsonResponse.success()
                .addResult("users-info", null)
                .build();
    }

    private AdminAuth authenticateRealmAdminRequest(RealmModel realm) {
        String tokenString = new AppAuthManager().extractAuthorizationHeaderToken(session.getContext().getRequestHeaders());
        if (tokenString == null) throw new NotAuthorizedException("Bearer");
        AccessToken token;
        try {
            JWSInput input = new JWSInput(tokenString);
            token = input.readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            throw new NotAuthorizedException("Bearer token format error");
        }

        String realmName = token.getIssuer().substring(token.getIssuer().lastIndexOf('/') + 1);
        RealmManager realmManager = new RealmManager(session);
        RealmModel realmFromToken = realmManager.getRealmByName(realmName);
        if (realmFromToken == null) {
            throw new NotAuthorizedException("Unknown realm in token");
        }

        session.getContext().setRealm(realm);
        AuthenticationManager.AuthResult authResult = new AppAuthManager()
                .authenticateBearerToken(session, realm, session.getContext().getUri(), session.getContext().getConnection(), session.getContext().getRequestHeaders());
        if (authResult == null) {
            log.debug("Token not valid");
            throw new NotAuthorizedException("Bearer");
        }

        ClientModel client = realm.getClientByClientId(token.getIssuedFor());
        if (client == null) {
            throw new NotAuthorizedException("Could not find client for authorization");
        }

        AdminAuth auth = new AdminAuth(realm, authResult.getToken(), authResult.getUser(), client);

        AdminPermissions.evaluator(session, realm, auth).users().requireManage();

        if (!auth.getRealm().equals(realmManager.getKeycloakAdminstrationRealm())
                && !auth.getRealm().equals(realm)) {
            throw new ForbiddenException();
        }

        return auth;
    }*/
}
