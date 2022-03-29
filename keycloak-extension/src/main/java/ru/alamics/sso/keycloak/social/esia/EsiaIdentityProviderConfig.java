package ru.alamics.sso.keycloak.social.esia;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;


public class EsiaIdentityProviderConfig extends OAuth2IdentityProviderConfig {

    private final static String ESIA_DOMAIN_URL = "esia.domain.url";

    private ApplicationProperties properties;

    public EsiaIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
        properties = Lookup.lookup(ApplicationProperties.class);
    }

    public String getEsiaDomainUrl() {
        return getConfig().getOrDefault("esiaDomainUrl", properties.getProperty(ESIA_DOMAIN_URL));
    }

    public void setEsiaDomainUrl(String url) {
        getConfig().put("esiaDomainUrl", url);
    }

}