package ru.alamics.sso.keycloak.social.esia;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;


public class EsiaIdentityProviderConfig extends OAuth2IdentityProviderConfig {

	public EsiaIdentityProviderConfig(IdentityProviderModel model) {
		super(model);
	}

	public String getEsiaDomainUrl() {
		return getConfig().getOrDefault("esiaDomainUrl", "https://esia-portal1.test.gosuslugi.ru");
	}

	public void setEsiaDomainUrl(String url) {
		getConfig().put("esiaDomainUrl", url);
	}

}