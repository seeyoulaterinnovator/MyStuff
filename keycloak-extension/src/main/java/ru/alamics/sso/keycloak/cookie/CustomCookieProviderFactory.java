package ru.alamics.sso.keycloak.cookie;

import org.keycloak.cookie.CookieProvider;
import org.keycloak.cookie.DefaultCookieProviderFactory;
import org.keycloak.models.KeycloakSession;

public class CustomCookieProviderFactory extends DefaultCookieProviderFactory {
    @Override
    public CookieProvider create(KeycloakSession session) {
        return new CustomCookieProvider(session, super.create(session));
    }

    @Override
    public String getId() {
        return "custom";
    }
}
