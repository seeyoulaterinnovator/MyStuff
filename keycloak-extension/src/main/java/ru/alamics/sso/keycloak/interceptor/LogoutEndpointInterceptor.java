package ru.alamics.sso.keycloak.interceptor;

import io.vertx.ext.auth.impl.jose.JWT;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.TokenVerifier;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.Urls;
import ru.alamics.sso.keycloak.GeneralRealm;

import java.io.IOException;

/**
 * Доработка для клиентов по типу ЛК, которые нарушают спецификацию OIDC
 * и передают в параметре id_token_hint access токен вместо ID токена
 * @see org.keycloak.protocol.oidc.endpoints.LogoutEndpoint
 */
@Provider
@PreMatching
@Slf4j
public class LogoutEndpointInterceptor implements ContainerRequestFilter {
    private static final String ID_TOKEN_HINT_PARAM = "id_token_hint";

    private static final String CLIENT_ID_PARAM = "client_id";

    @Context
    KeycloakSession session;

    public void filter(ContainerRequestContext requestContext) throws IOException {
        if(!(
                requestContext.getUriInfo().getPath().matches("/realms/[^/]+/protocol/openid-connect/logout")
                        && HttpMethod.GET.equals(requestContext.getMethod())
        )) return;

        String realmName = requestContext.getUriInfo().getPathSegments().get(1).toString();

        if(Config.getAdminRealm().equals(realmName) || GeneralRealm.MANAGER_REALMS.contains(realmName)) return;

        String idToken = requestContext.getUriInfo().getQueryParameters().getFirst(ID_TOKEN_HINT_PARAM);

        if(idToken == null) return;

        try {
            if("ID".equals(JWT.parse(idToken).getJsonObject("payload").getString("typ"))) return;
        } catch (Exception e) {
            log.trace(e.getMessage(), e);
        }

        var clientId = requestContext.getUriInfo().getQueryParameters().getFirst(CLIENT_ID_PARAM);

        var tokenClientId = verifyAccessTokenAndGetClientId(idToken, realmName);

        if(tokenClientId == null) return;

        if(clientId != null && !clientId.equals(tokenClientId)) return;

        requestContext.setRequestUri(
                requestContext.getUriInfo()
                        .getRequestUriBuilder()
                        .replaceQueryParam(ID_TOKEN_HINT_PARAM)
                        .replaceQueryParam(CLIENT_ID_PARAM, tokenClientId)
                        .build()
        );
    }

    String verifyAccessTokenAndGetClientId(String accessToken, String realmName) {
        @Cleanup var session = this.session.getKeycloakSessionFactory().create();

        try {
            var realm = session.realms().getRealmByName(realmName);

            if (realm == null) return null;

            session.getContext().setRealm(realm);

            @SuppressWarnings("deprecation")
            var verifier = TokenVerifier.create(accessToken, AccessToken.class)
                    .withDefaultChecks()
                    .realmUrl(Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realmName));

            session.getContext().setRealm(realm);

            var verifierContext = session.getProvider(
                            SignatureProvider.class,
                            verifier.getHeader().getAlgorithm().name()
                    )
                    .verifier(verifier.getHeader().getKeyId());

            verifier.verifierContext(verifierContext);

            var token = verifier.verify().getToken();

            if (token == null) return null;

            var client = realm.getClientByClientId(token.getIssuedFor());

            if (client == null) return null;

            return client.getClientId();
        } catch (Exception e) {
            log.trace(e.getMessage(), e);
            return null;
        }
    }
}
