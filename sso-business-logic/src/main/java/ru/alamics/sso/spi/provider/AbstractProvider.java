package ru.alamics.sso.spi.provider;

import org.keycloak.provider.Provider;

public interface AbstractProvider extends Provider {
    @Override
    default void close() {
    }
}
