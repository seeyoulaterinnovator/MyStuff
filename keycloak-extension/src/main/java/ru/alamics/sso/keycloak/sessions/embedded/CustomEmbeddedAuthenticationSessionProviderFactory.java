package ru.alamics.sso.keycloak.sessions.embedded;

import io.quarkus.arc.impl.Reflections;
import lombok.SneakyThrows;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.sessions.infinispan.InfinispanAuthenticationSessionProvider;
import org.keycloak.models.sessions.infinispan.InfinispanAuthenticationSessionProviderFactory;
import org.keycloak.sessions.AuthenticationSessionProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CustomEmbeddedAuthenticationSessionProviderFactory extends InfinispanAuthenticationSessionProviderFactory {
    private static final int PROVIDER_PRIORITY = 2;

    private static final String PROVIDER_ID = "custom-embedded";

    @Override
    public AuthenticationSessionProvider create(KeycloakSession session) {
        lazyInit(session);
        InfinispanAuthenticationSessionProvider provider = (InfinispanAuthenticationSessionProvider) super.create(session);
        return new CustomEmbeddedAuthenticationSessionProvider(session, provider);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public int order() {
        return PROVIDER_PRIORITY;
    }

    @Deprecated(forRemoval = true)
    @SneakyThrows({
            IllegalAccessException.class,
            IllegalArgumentException.class,
            InvocationTargetException.class
    })
    private void lazyInit(KeycloakSession session) {
        Method method = Reflections.findMethod(getClass().getSuperclass(), "lazyInit", KeycloakSession.class);
        method.setAccessible(true);
        method.invoke(this, session);
    }

}
