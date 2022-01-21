package ru.alamics.sso.keycloak.social.vk;

import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;

public class VkUserAttributeMapper extends AbstractJsonUserAttributeMapper {

    private static final String[] cp = new String[]{VkIdentityProviderFactory.PROVIDER_ID};
    private static final String MAPPER_ID = "vk-user-attribute-mapper";

    @Override
    public String[] getCompatibleProviders() {
        return cp;
    }

    @Override
    public String getId() {
        return MAPPER_ID;
    }
}
