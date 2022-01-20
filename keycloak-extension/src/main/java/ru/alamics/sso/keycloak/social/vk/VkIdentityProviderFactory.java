package ru.alamics.sso.keycloak.social.vk;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

public class VkIdentityProviderFactory extends AbstractIdentityProviderFactory<VkIdentityProvider> implements SocialIdentityProviderFactory<VkIdentityProvider> {

    protected static final String PROVIDER_ID = "vkontakte";
    private static final String DISPLAY_NAME = "VK";

    @Override
    public String getName() {
        return DISPLAY_NAME;
    }

    @Override
    public VkIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new VkIdentityProvider(session, new OAuth2IdentityProviderConfig(model));
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}