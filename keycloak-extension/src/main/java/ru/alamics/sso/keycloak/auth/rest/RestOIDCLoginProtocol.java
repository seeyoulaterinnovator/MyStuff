package ru.alamics.sso.keycloak.auth.rest;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.util.TokenUtil;
import ru.alamics.sso.util.Util;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
@Slf4j
public class RestOIDCLoginProtocol extends OIDCLoginProtocol {
    @Override
    public Response authenticated(AuthenticationSessionModel authSession, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        if (Util.isPasswordGrandType(session)) {
            return createTokenResponse(authSession, userSession);
        }
        return super.authenticated(authSession, userSession, clientSessionCtx);
    }

    private Response createTokenResponse(AuthenticationSessionModel authSession, UserSessionModel userSession) {
        log.info("createTokenResponse is called");
        AuthenticationManager.setClientScopesInSession(authSession);
        ClientSessionContext clientSessionContext = TokenManager.attachAuthenticationSession(session, userSession, authSession);
        TokenManager.AccessTokenResponseBuilder responseBuilder = new TokenManager().responseBuilder(realm, authSession.getClient(), event, session, userSession, clientSessionContext)
                .generateAccessToken()
                .generateRefreshToken();

        String scopeParam = clientSessionContext.getClientSession().getNote(OAuth2Constants.SCOPE);
        if (TokenUtil.isOIDCRequest(scopeParam)) {
            responseBuilder.generateIDToken();
        }
        return Response.ok(responseBuilder.build(), MediaType.APPLICATION_JSON_TYPE).build();
    }
}
