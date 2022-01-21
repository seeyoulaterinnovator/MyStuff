package ru.alamics.sso.keycloak.social.vk;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;

@Slf4j
public class VkIdentityProvider extends AbstractOAuth2IdentityProvider<OAuth2IdentityProviderConfig> implements SocialIdentityProvider<OAuth2IdentityProviderConfig> {
    private static final String OAUTH2_PARAMETER_EMAIL = "email";

    private static final String AUTH_URL = "vk.auth.url";
    private static final String TOKEN_URL = "vk.token.url";
    private static final String PROFILE_URL = "vk.profile.url";
    private static final String DEFAULT_SCOPE = "vk.default.scope";
    private static final String VK_API_VERSION = "vk.api.version";

    private ApplicationProperties properties;

    VkIdentityProvider(KeycloakSession session, OAuth2IdentityProviderConfig config) {
        super(session, config);

        config.setAuthorizationUrl(properties.getProperty(AUTH_URL));
        config.setTokenUrl(properties.getProperty(TOKEN_URL));
        config.setUserInfoUrl(properties.getProperty(PROFILE_URL));
    }

    public BrokeredIdentityContext getFederatedIdentity(String response) {
        log.info("start vk provider login");
        String accessToken = extractTokenFromResponse(response, OAUTH2_PARAMETER_ACCESS_TOKEN);
        String email = extractTokenFromResponse(response, OAUTH2_PARAMETER_EMAIL);
        if (accessToken == null) {
            throw new IdentityBrokerException("No access token available in OAuth server response: " + response);
        }
        return doGetFederatedIdentity(accessToken, email);
    }

    private BrokeredIdentityContext doGetFederatedIdentity(String accessToken, String email) {
        try {
            JsonNode profile = SimpleHttp.doGet(properties.getProperty(PROFILE_URL), session)
                    .param("access_token", accessToken)
                    .param("v", properties.getProperty(VK_API_VERSION))
                    .param("response_type", "token")
                    .param("fields", "domain")
                    .asJson();
            return extractIdentityFromProfile(profile, email);
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not obtain user profile from vk.", e);
        }
    }

    private BrokeredIdentityContext extractIdentityFromProfile(JsonNode profileJson, String email) {
        JsonNode profile = profileJson.findValue("response").get(0);

        String id = getJsonProperty(profile, "id");

        BrokeredIdentityContext user = new BrokeredIdentityContext(id);

        String username = getJsonProperty(profile, "domain");
        String firstName = getJsonProperty(profile, "first_name");
        String lastName = getJsonProperty(profile, "last_name");
        if (username == null) {
            username = email;
            if (username == null)
                username = id;
        }
        if (lastName == null) {
            lastName = "";
        } else {
            lastName = " " + lastName;
        }

        user.setEmail(email);
        user.setUsername(username);
        user.setName(firstName + lastName);
        user.setIdpConfig(getConfig());
        user.setIdp(this);
        user.getContextData().put("firstName", firstName + " " + lastName);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());
        return user;
    }

    @Override
    protected String getDefaultScopes() {
        properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);
        return properties.getProperty(DEFAULT_SCOPE);
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new AbstractOAuth2IdentityProvider.Endpoint(callback, realm, event);
    }
}