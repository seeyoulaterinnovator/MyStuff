package ru.alamics.sso.keycloak.social.vk;

import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;

public class VkUserAttributeMapper extends AbstractJsonUserAttributeMapper {

    private static final String[] cp = new String[]{VkIdentityProviderFactory.PROVIDER_ID};

    @Override
    public String[] getCompatibleProviders() {
        return cp;
    }

    @Override
    public String getId() {
        return "vk-user-attribute-mapper";
    }
}
