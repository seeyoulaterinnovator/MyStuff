package ru.alamics.sso.keycloak.interceptor;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.ext.auth.impl.jose.JWT;
import jakarta.inject.Inject;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.keycloak.Config;
import org.keycloak.TokenVerifier;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.Urls;
import ru.alamics.sso.client.ClientService;
import ru.alamics.sso.keycloak.GeneralRealm;

import static ru.alamics.sso.keycloak.util.MiscUtil.notEmpty;

/**
 * Доработка для клиентов по типу ЛК, которые нарушают спецификацию OIDC
 * и передают в параметре id_token_hint access токен вместо ID токена
 * @see org.keycloak.protocol.oidc.endpoints.LogoutEndpoint
 */
@Provider
@PreMatching
@Slf4j
public class LogoutEndpointInterceptor {
    private static final String ID_TOKEN_HINT_PARAM = "id_token_hint";

    private static final String CLIENT_ID_PARAM = "client_id";

    private static final String POST_LOGOUT_REDIRECT_URI_PARAM = "post_logout_redirect_uri";

    @Context
    KeycloakSession session;

    @Inject
    ClientService clientService;

    @ServerRequestFilter(preMatching = true)
    public Uni<Void> filter(ContainerRequestContext requestContext) {
        var result = Uni.createFrom().voidItem();

        if(!(
                requestContext.getUriInfo().getPath().matches("/realms/[^/]+/protocol/openid-connect/logout")
                        && HttpMethod.GET.equals(requestContext.getMethod())
        )) return result;

        String realmName = requestContext.getUriInfo().getPathSegments().get(1).toString();

        if(Config.getAdminRealm().equals(realmName) || GeneralRealm.MANAGER_REALMS.contains(realmName)) return result;

        String idToken = requestContext.getUriInfo().getQueryParameters().getFirst(ID_TOKEN_HINT_PARAM);
        var clientId = requestContext.getUriInfo().getQueryParameters().getFirst(CLIENT_ID_PARAM);
        String redirectUri = requestContext.getUriInfo().getQueryParameters().getFirst(POST_LOGOUT_REDIRECT_URI_PARAM);

        if(idToken == null) return result;

        try {
            if("ID".equals(JWT.parse(idToken).getJsonObject("payload").getString("typ"))) return result;
        } catch (Exception e) {
            log.trace(e.getMessage(), e);
        }

        return Uni.createFrom().voidItem().chain(Unchecked.supplier(() -> {
                    var client = verifyAccessTokenAndGetClient(idToken, realmName);

                    if (client == null) return result;

                    if (clientId != null && !clientId.equals(client.getId())) return result;

                    String newRedirectUri = redirectUri;
                    if (newRedirectUri == null || newRedirectUri.isBlank()) {
                        newRedirectUri = notEmpty(
                                clientService.getMainRedirectUri(client.getClientId()),
                                client.getBaseUrl(),
                                client.getRootUrl()
                        );
                    }

                    requestContext.setRequestUri(
                            requestContext.getUriInfo()
                                    .getRequestUriBuilder()
                                    .replaceQueryParam(ID_TOKEN_HINT_PARAM)
                                    .replaceQueryParam(CLIENT_ID_PARAM, client.getClientId())
                                    .replaceQueryParam(POST_LOGOUT_REDIRECT_URI_PARAM, newRedirectUri)
                                    .build()
                    );

                    return result;
                }))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .replaceWith(result);
    }

    ClientModel verifyAccessTokenAndGetClient(String accessToken, String realmName) {
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

            return realm.getClientByClientId(token.getIssuedFor());
        } catch (Exception e) {
            log.trace(e.getMessage(), e);
            return null;
        }
    }
}
