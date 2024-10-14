package ru.alamics.sso.keycloak.oidc;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.TokenVerifier;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.Urls;
import ru.alamics.sso.keycloak.GeneralRealm;

import java.io.IOException;

/**
 * Доработка для клиентов по типу ЛК, которые нарушают спецификацию OIDC
 * и передают в параметре id_token_hint access токен вместо ID токена
 */
@Provider
@PreMatching
@Slf4j
public class LogoutEndpointInterceptor implements ContainerRequestFilter {
    private static final String ID_TOKEN_HINT_PARAM = "id_token_hint";

    private static final String CLIENT_ID_PARAM = "client_id";

    @Context
    KeycloakSession session;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if(!(requestContext.getUriInfo().getPath().matches("/realms/[^/]+/protocol/openid-connect/logout")
                && HttpMethod.GET.equals(requestContext.getMethod()))) return;

        String realmName = requestContext.getUriInfo().getPathSegments().get(1).toString();

        if(Config.getAdminRealm().equals(realmName) || GeneralRealm.MANAGER_REALMS.contains(realmName)) return;

        String idToken = requestContext.getUriInfo().getQueryParameters().getFirst(ID_TOKEN_HINT_PARAM);

        if(idToken == null) return;

        var clientId = requestContext.getUriInfo().getQueryParameters().getFirst(CLIENT_ID_PARAM);

        var oldRealm = session.getContext().getRealm();
        try {
            TokenVerifier<AccessToken> verifier = TokenVerifier.create(idToken, AccessToken.class)
                    .withDefaultChecks()
                    .realmUrl(Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realmName));

            RealmModel realm = session.getProvider(RealmProvider.class).getRealmByName(realmName);

            if(realm == null) return;

            session.getContext().setRealm(realm);

            var verifierContext = session.getProvider(
                            SignatureProvider.class,
                            verifier.getHeader().getAlgorithm().name()
                    )
                    .verifier(verifier.getHeader().getKeyId());

            verifier.verifierContext(verifierContext);

            AccessToken token = verifier.verify().getToken();

            if(token == null) return;

            ClientModel client = realm.getClientByClientId(token.getIssuedFor());

            if(client == null) return;

            if(clientId == null) clientId = client.getClientId();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return;
        } finally {
            session.getContext().setRealm(oldRealm);
        }

        requestContext.setRequestUri(
                requestContext.getUriInfo()
                        .getRequestUriBuilder()
                        .replaceQueryParam(ID_TOKEN_HINT_PARAM)
                        .replaceQueryParam(CLIENT_ID_PARAM, clientId)
                        .build()
        );
    }
}
