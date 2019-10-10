package ru.alamics.sso.keycloak.social.esia;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.phone.ActivationCodeType;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@Slf4j
public class EsiaIdentityProviderFactory extends AbstractIdentityProviderFactory<EsiaIdentityProvider> implements SocialIdentityProviderFactory<EsiaIdentityProvider> {

    public static final String PROVIDER_ID = "esia";

    @Override
    public String getName() {
        return "Esia";
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
