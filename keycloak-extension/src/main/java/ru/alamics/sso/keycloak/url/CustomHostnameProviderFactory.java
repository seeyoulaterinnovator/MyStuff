package ru.alamics.sso.keycloak.url;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.url.HostnameV2Provider;
import org.keycloak.url.HostnameV2ProviderFactory;
import org.keycloak.urls.HostnameProvider;
import org.keycloak.urls.HostnameSpi;

public class CustomHostnameProviderFactory extends HostnameV2ProviderFactory {
    @Override
    public void init(Config.Scope config) {
        super.init(Config.scope(new HostnameSpi().getName(), super.getId()));
    }

    @Override
    public HostnameProvider create(KeycloakSession session) {
        return new CustomHostnameProvider((HostnameV2Provider) super.create(session));
    }

    @Override
    public String getId() {
        return "custom";
    }
}
