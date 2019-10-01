package ru.alamics.sso.keycloak.auth.link.rest;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionToken;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionTokenHandler;
import org.keycloak.authorization.policy.evaluation.Realm;
import org.keycloak.common.util.Time;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import ru.alamics.sso.keycloak.auth.link.token.AuthLinkActionToken;
import ru.alamics.sso.keycloak.response.JsonResponse;


import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Slf4j
public class AuthLinkResource {

    protected KeycloakSession session;

    public AuthLinkResource(KeycloakSession session) {
        this.session = session;
    }

    @GET
    @Path("/{id}/{client}")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getLink(@PathParam("id") String userId, @PathParam("client") String clientId) {

        RealmManager realmManager = new RealmManager(session);
        RealmModel realm = realmManager.getRealmByName("user");
        if (realm == null)
            throw new NotFoundException("Realm not found.");

        ClientModel clientModel = session.clientStorageManager().getClientByClientId(clientId, realm);
        if (clientModel == null)
            throw new NotFoundException("Client not found.");

        log.info("Request for auth link, user " + userId + ", client " + clientId);

        //log.info("got client " + clientModel.getId() + " " + clientModel.getName() + " " + clientModel.getClientId());

        RootAuthenticationSessionModel rootAuthenticationSessionModel = session.authenticationSessions().createRootAuthenticationSession(realm);
        AuthenticationSessionModel authenticationSession = rootAuthenticationSessionModel.createAuthenticationSession(clientModel);
        //log.info("got authenticationSession " + authenticationSession.toString());



        int validityInSecs = realm.getActionTokenGeneratedByUserLifespan(ResetCredentialsActionToken.TOKEN_TYPE);
        int absoluteExpirationInSecs = Time.currentTime() + validityInSecs;

        // We send the secret in the email in a link as a query param.
        String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authenticationSession).getEncodedId();

        AuthLinkActionToken token = new AuthLinkActionToken(userId, absoluteExpirationInSecs, authSessionEncodedId);
        token.issuedFor(clientModel.getClientId());

        UriInfo uriInfo = session.getContext().getUri();

        UriBuilder builder = Urls.actionTokenBuilder(uriInfo.getBaseUri(), token.serialize(session, realm, uriInfo),
                clientModel.getClientId(), authenticationSession.getTabId())
        ;

        String link = builder.build(realm.getName()).toString();

        return JsonResponse.success()
                .addResult("link", link)
                .build();
    }

    public static class AuthLinkAuthenticationProcessor extends AuthenticationProcessor {

    }
}
