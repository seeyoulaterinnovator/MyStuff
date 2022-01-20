package ru.alamics.sso.keycloak.social.esia;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

@Slf4j
public class EsiaIdentityProviderFactory extends AbstractIdentityProviderFactory<EsiaIdentityProvider> implements SocialIdentityProviderFactory<EsiaIdentityProvider> {

    private static final String PROVIDER_ID = "esia";
    private static final String DISPLAY_NAME = "Esia";

    @Override
    public String getName() {
        return DISPLAY_NAME;
    }

    @Override
    public EsiaIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new EsiaIdentityProvider(session, new EsiaIdentityProviderConfig(model));
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
