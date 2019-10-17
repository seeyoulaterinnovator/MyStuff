package ru.alamics.sso.keycloak.social.esia;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.endpoints.AuthorizationEndpoint;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.util.Signer;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.http.HttpRequest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class EsiaIdentityProvider extends AbstractOAuth2IdentityProvider<EsiaIdentityProviderConfig> implements SocialIdentityProvider<EsiaIdentityProviderConfig> {
    private static final String AUTH_URL = "/aas/oauth2/ac";
    private static final String TOKEN_URL = "/aas/oauth2/te";
    private static final String PROFILE_URL = "/rs/prns/";

    private static final String DEFAULT_SCOPE = "openid fullname";
    private static final String TIMESTAMP = "timestamp";
    private static final String ACCESS_TYPE = "access_type";
    private static final DateTimeFormatter timestampFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss Z");

    private static final Map<String, String> uuidToState = new ConcurrentHashMap<>();

    EsiaIdentityProvider(KeycloakSession session, EsiaIdentityProviderConfig config) {
        super(session, config);
        config.setAuthorizationUrl(config.getEsiaDomainUrl() + AUTH_URL);
        config.setTokenUrl(config.getEsiaDomainUrl() + TOKEN_URL);
        config.setUserInfoUrl(config.getEsiaDomainUrl() + PROFILE_URL);
    }

    @Override
    protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
        final String dateTime = timestampFormat.format(ZonedDateTime.now());
        final UUID uuid = UUID.randomUUID();

        final UriBuilder uriBuilder = UriBuilder.fromUri(getConfig().getAuthorizationUrl())
                .queryParam(OAUTH2_PARAMETER_CLIENT_ID, getConfig().getClientId())
                .queryParam(OAUTH2_PARAMETER_CLIENT_SECRET,
                        Signer.signString(getConfig().getDefaultScope() + dateTime + getConfig().getClientId() + uuid))
                .queryParam(OAUTH2_PARAMETER_REDIRECT_URI, request.getRedirectUri())
                .queryParam(OAUTH2_PARAMETER_SCOPE, getDefaultScopes())
                .queryParam(OAUTH2_PARAMETER_RESPONSE_TYPE, "code")
                .queryParam(OAUTH2_PARAMETER_STATE, uuid)
                .queryParam(TIMESTAMP, dateTime)
                .queryParam(ACCESS_TYPE, "online");

        String loginHint = request.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);
        if (getConfig().isLoginHint() && loginHint != null) {
            uriBuilder.queryParam(OIDCLoginProtocol.LOGIN_HINT_PARAM, loginHint);
        }

        if (getConfig().isUiLocales()) {
            uriBuilder.queryParam(OIDCLoginProtocol.UI_LOCALES_PARAM, session.getContext().resolveLocale(null).toLanguageTag());
        }

        String prompt = getConfig().getPrompt();
        if (prompt == null || prompt.isEmpty()) {
            prompt = request.getAuthenticationSession().getClientNote(OAuth2Constants.PROMPT);
        }
        if (prompt != null) {
            uriBuilder.queryParam(OAuth2Constants.PROMPT, prompt);
        }

        String nonce = request.getAuthenticationSession().getClientNote(OIDCLoginProtocol.NONCE_PARAM);
        if (nonce == null || nonce.isEmpty()) {
            nonce = UUID.randomUUID().toString();
            request.getAuthenticationSession().setClientNote(OIDCLoginProtocol.NONCE_PARAM, nonce);
        }
        uriBuilder.queryParam(OIDCLoginProtocol.NONCE_PARAM, nonce);

        String acr = request.getAuthenticationSession().getClientNote(OAuth2Constants.ACR_VALUES);
        if (acr != null) {
            uriBuilder.queryParam(OAuth2Constants.ACR_VALUES, acr);
        }
        String forwardParameterConfig = getConfig().getForwardParameters() != null ? getConfig().getForwardParameters() : "";
        List<String> forwardParameters = Arrays.asList(forwardParameterConfig.split("\\s*,\\s*"));
        for (String forwardParameter : forwardParameters) {
            String name = AuthorizationEndpoint.LOGIN_SESSION_NOTE_ADDITIONAL_REQ_PARAMS_PREFIX + forwardParameter.trim();
            String parameter = request.getAuthenticationSession().getClientNote(name);
            if (parameter != null && !parameter.isEmpty()) {
                uriBuilder.queryParam(forwardParameter, parameter);
            }
        }

        uuidToState.put(uuid.toString(), request.getState().getEncoded());
        /*
            TODO после перехода на новый Keycloak
            AuthenticationSessionModel asm = session.getContext().getAuthenticationSession();
            asm.setAuthNote(uuid.toString(), request.getState().getEncoded());
        */

        return uriBuilder;
    }

    protected class Endpoint {
        protected AuthenticationCallback callback;
        protected RealmModel realm;
        protected EventBuilder event;

        @Context
        protected ClientConnection clientConnection;

        @Context
        protected HttpHeaders headers;

        public Endpoint(AuthenticationCallback callback, RealmModel realm, EventBuilder event) {
            this.callback = callback;
            this.realm = realm;
            this.event = event;
        }

        @GET
        public Response authResponse(@QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_STATE) String state,
                                     @QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CODE) String authorizationCode,
                                     @QueryParam(OAuth2Constants.ERROR) String error) {

            log.info("esia authResponse state={}, authorizationCode={}, error", state, authorizationCode, error);
            if (error != null) {
                //logger.error("Failed " + getConfig().getAlias() + " broker login: " + error);
                if (error.equals(ACCESS_DENIED)) {
                    logger.error(ACCESS_DENIED + " for broker login " + getConfig().getProviderId());
                    return callback.cancelled(state);
                } else {
                    logger.error(error + " for broker login " + getConfig().getProviderId());
                    return callback.error(state, Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
                }
            }

            try {

                if (authorizationCode != null) {
                    String response = generateTokenRequest(authorizationCode, state).asString();

                    log.info("response marker = {}", response);


                    BrokeredIdentityContext federatedIdentity = getFederatedIdentity(response);

                    if (getConfig().isStoreToken()) {
                        // make sure that token wasn't already set by getFederatedIdentity();
                        // want to be able to allow provider to set the token itself.
                        if (federatedIdentity.getToken() == null) federatedIdentity.setToken(response);
                    }

                    federatedIdentity.setIdpConfig(getConfig());
                    federatedIdentity.setIdp(EsiaIdentityProvider.this);
                    federatedIdentity.setCode(uuidToState.remove(state));

                    /*  TODO после перехода на новый Keycloak
                        AuthenticationSessionModel asm = session.getContext().getAuthenticationSession();
                        federatedIdentity.setCode(asm.getAuthNote(state));
                        asm.removeAuthNote(state);
                    */

                    return callback.authenticated(federatedIdentity);
                }
            } catch (WebApplicationException e) {
                return e.getResponse();
            } catch (Exception e) {
                logger.error("Failed to make identity provider oauth callback", e);
            }
            event.event(EventType.LOGIN);
            event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
            return ErrorPage.error(session, null, Response.Status.BAD_GATEWAY, Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
        }

        private SimpleHttp generateTokenRequest(String authorizationCode, String state) {
            final String dateTime = timestampFormat.format(ZonedDateTime.now());
            final UUID uuid = UUID.randomUUID();

            return SimpleHttp.doPost(getConfig().getTokenUrl(), session)
                    .param(OAUTH2_PARAMETER_CLIENT_ID, getConfig().getClientId())
                    .param(OAUTH2_PARAMETER_CLIENT_SECRET,
                            Signer.signString(getDefaultScopes() + dateTime + getConfig().getClientId() + uuid))
                    .param(OAUTH2_PARAMETER_REDIRECT_URI, session.getContext().getUri().getAbsolutePath().toString())
                    .param(OAUTH2_PARAMETER_SCOPE, getDefaultScopes())
                    .param(OAUTH2_PARAMETER_STATE, uuid.toString())
                    .param(TIMESTAMP, dateTime)
                    .param(OAUTH2_PARAMETER_CODE, authorizationCode)
                    .param(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE)
                    .param("token_type", "Bearer");
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new Endpoint(callback, realm, event);
    }

    private BrokeredIdentityContext extractIdentityFromProfile(JsonNode profile, String userId) {
        log.info("profile={}", profile);
        BrokeredIdentityContext user = new BrokeredIdentityContext(userId);

//        String email = getJsonProperty(profile, "email");
//
//        user.setEmail(email);


        String firstName = getJsonProperty(profile, "firstName");
        String lastName = getJsonProperty(profile, "lastName");
        String middleName = getJsonProperty(profile, "middleName");

        String username = firstName + " " + lastName;
        if (!Validation.isBlank(middleName)) {
            username += " " + middleName;
        }
        user.setName(firstName + lastName);
        user.setFirstName(username);
        user.setLastName("-");
        user.setUsername(username);
        user.getContextData().put("firstName", username);

        user.setIdpConfig(getConfig());
        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());

        return user;
    }

    @Override
    public BrokeredIdentityContext getFederatedIdentity(String response) {
        String accessToken = extractTokenFromResponse(response, getAccessTokenResponseParameter());

        if (accessToken == null) {
            throw new IdentityBrokerException("No access token available in OAuth server response: " + response);
        }

        BrokeredIdentityContext context = doGetFederatedIdentity(accessToken);
        context.getContextData().put(FEDERATED_ACCESS_TOKEN, accessToken);
        return context;
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        try {
            DecodedToken decodedToken = DecodedToken.getDecoded(accessToken);
            log.info("decodedToken = {}", decodedToken);

            JsonNode jsonNodeProfile = SimpleHttp.doGet(getConfig().getUserInfoUrl() + decodedToken.getUserId(), session)
                    .header("Authorization", "Bearer " + accessToken)
                    .asJson();
            return extractIdentityFromProfile(jsonNodeProfile, decodedToken.getUserId());
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not obtain user profile from esia.", e);
        }
    }
}