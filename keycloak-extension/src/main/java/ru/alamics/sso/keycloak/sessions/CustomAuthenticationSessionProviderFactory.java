package ru.alamics.sso.keycloak.sessions;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.sessions.infinispan.InfinispanAuthenticationSessionProviderFactory;
import org.keycloak.sessions.AuthenticationSessionProvider;

public class CustomAuthenticationSessionProviderFactory extends InfinispanAuthenticationSessionProviderFactory {
    @Override
    public AuthenticationSessionProvider create(KeycloakSession session) {
        return new CustomAuthenticationSessionProvider(session, super.create(session));
    }

    @Override
    public String getId() {
        return "custom";
    }
}
