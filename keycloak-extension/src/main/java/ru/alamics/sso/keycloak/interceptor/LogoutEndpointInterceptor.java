package ru.alamics.sso.keycloak.interceptor;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.ext.Provider;
import lombok.Builder;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.keycloak.Config;
import org.keycloak.TokenVerifier;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserProvider;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import ru.alamics.sso.client.ClientService;
import ru.alamics.sso.jpa.repository.UserPostRepository;
import ru.alamics.sso.keycloak.GeneralRealm;
import ru.alamics.sso.keycloak.util.HttpUtil;
import ru.alamics.sso.keycloak.util.MiscUtil;
import ru.alamics.sso.keycloak.util.TokenUtil;
import ru.alamics.sso.property.ApplicationProperties;

import static org.keycloak.util.TokenUtil.TOKEN_TYPE_ID;
import static ru.alamics.sso.keycloak.util.MiscUtil.hasText;

/**
 * Доработка для клиентов по типу ЛК, которые нарушают спецификацию OIDC
 * и передают в параметре id_token_hint access токен вместо ID токена
 * @see org.keycloak.protocol.oidc.endpoints.LogoutEndpoint
 */
@Provider
@Slf4j
public class LogoutEndpointInterceptor implements ContainerResponseFilter {
    private static final String ID_TOKEN_HINT_PARAM = "id_token_hint";

    private static final String CLIENT_ID_PARAM = "client_id";

    private static final String POST_LOGOUT_REDIRECT_URI_PARAM = "post_logout_redirect_uri";

    private static final String CORS_ORIGIN_PROPERTY = LogoutEndpointInterceptor.class.getName() + "CorsOrigin";

    @Context
    KeycloakSession keycloak;

    @Inject
    ApplicationProperties properties;

    @Inject
    ClientService clientService;

    @Inject
    UserPostRepository userPostRepository;

    @ServerRequestFilter(preMatching = true)
    public Uni<Void> asyncFilter(ContainerRequestContext requestContext) {
        return Uni.createFrom().voidItem().invoke(() -> {
            try {
                filter(requestContext);
            } catch (Exception e) {
                log.debug(e.getMessage(), e);
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    void filter(ContainerRequestContext requestContext) throws Exception {
        if (!(
                requestContext.getUriInfo().getPath().matches("/realms/[^/]+/protocol/openid-connect/logout")
                        && HttpMethod.GET.equals(requestContext.getMethod())
        )) return;

        String realmName = requestContext.getUriInfo().getPathSegments().get(1).toString();

        if(Config.getAdminRealm().equals(realmName) || GeneralRealm.MANAGER_REALMS.contains(realmName)) return;

        var token = requestContext.getUriInfo().getQueryParameters().getFirst(ID_TOKEN_HINT_PARAM);
        var clientId = requestContext.getUriInfo().getQueryParameters().getFirst(CLIENT_ID_PARAM);
        var redirectUri = requestContext.getUriInfo().getQueryParameters().getFirst(POST_LOGOUT_REDIRECT_URI_PARAM);

        if (TokenUtil.isIdToken(token)) return;

        var idToken = verifyAccessTokenAndGetIdToken(token, realmName, clientId);

        if (idToken == null) return;

        if (!hasText(redirectUri)) redirectUri = getClientRedirectUri(realmName, idToken.clientId);

        String origin = null;
        try {
            var referer = requestContext.getHeaderString("Referer");
            var secFetchSite = requestContext.getHeaderString("Sec-Fetch-Site");
            var secFetchMode = requestContext.getHeaderString("Sec-Fetch-Mode");
            if ("cross-site".equals(secFetchSite) && "cors".equals(secFetchMode) && hasText(referer)) {
                if (HttpUtil.isSameDomain(
                        requestContext.getUriInfo().getBaseUri().toString(),
                        referer,
                        properties.getPropertyInt("logout.sameDomainLevel", 2)
                )) {
                    origin = UriBuilder.fromUri(referer).replacePath("").replaceQuery("").build().toString();
                }
            }
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
        if (origin != null) {
            requestContext.setProperty(CORS_ORIGIN_PROPERTY, origin);
        }

        requestContext.setRequestUri(
                requestContext.getUriInfo()
                        .getRequestUriBuilder()
                        .replaceQueryParam(ID_TOKEN_HINT_PARAM, idToken.token)
                        .replaceQueryParam(POST_LOGOUT_REDIRECT_URI_PARAM, redirectUri)
                        .build()
        );
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        var corsOrigin = (String) requestContext.getProperty(CORS_ORIGIN_PROPERTY);
        if (corsOrigin != null && responseContext.getStatus() == HttpStatus.SC_MOVED_TEMPORARILY) {
            responseContext.getHeaders().clear();
            responseContext.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
            responseContext.getHeaders().add("Access-Control-Allow-Origin", corsOrigin);
            responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET");
            responseContext.setStatus(HttpStatus.SC_NO_CONTENT);
            responseContext.setEntity("");
            AuthenticationManager.expireIdentityCookie(keycloak);
            AuthenticationManager.expireAuthSessionCookie(keycloak);
        }
    }

    IdToken verifyAccessTokenAndGetIdToken(String accessToken, String realmName, String clientId) throws Exception {
        @Cleanup var keycloak = this.keycloak.getKeycloakSessionFactory().create();

        var realm = keycloak.realms().getRealmByName(realmName);
        if (realm == null) return null;

        keycloak.getContext().setRealm(realm);

        @SuppressWarnings("deprecation")
        var verifier = TokenVerifier.create(accessToken, AccessToken.class)
                .withDefaultChecks()
                .realmUrl(Urls.realmIssuer(keycloak.getContext().getUri().getBaseUri(), realmName));
        var verifierContext = keycloak.getProvider(
                        SignatureProvider.class,
                        verifier.getHeader().getAlgorithm().name()
                )
                .verifier(verifier.getHeader().getKeyId());
        verifier.verifierContext(verifierContext);

        var token = verifier.verify().getToken();
        if (token == null) return null;

        var client = realm.getClientByClientId(token.getIssuedFor());
        if (client == null || clientId != null && !clientId.equals(client.getClientId())) return null;

        var users = keycloak.getProvider(UserProvider.class);
        var user = users.getUserById(realm, token.getSubject());
        if (user == null) {
            var post = userPostRepository.getUserPost(token.getSubject());
            if (post != null) user = users.getUserById(realm, post.getUser().getId());
        }
        if (user == null) return null;

        var session = keycloak.sessions().getUserSession(realm, token.getSessionId());
        if (session == null) return null;

        var idToken = AuthenticationManager.createIdentityToken(keycloak, realm, user, session, token.getIssuer());
        idToken.type(TOKEN_TYPE_ID);
        idToken.issuedFor(client.getClientId());
        return IdToken.builder()
                .clientId(client.getClientId())
                .token(keycloak.tokens().encode(idToken))
                .build();
    }

    String getClientRedirectUri(String realmName, String clientId) {
        @Cleanup var keycloak = this.keycloak.getKeycloakSessionFactory().create();
        var client = keycloak.realms().getRealm(realmName).getClientByClientId(clientId);
        return MiscUtil.notEmpty(
                clientService.getMainRedirectUri(client.getId()),
                client.getBaseUrl(),
                client.getRootUrl()
        );
    }

    @Builder
    @RequiredArgsConstructor
    static class IdToken {
        final String clientId;

        final String token;
    }
}
