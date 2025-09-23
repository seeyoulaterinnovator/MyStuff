package ru.alamics.sso.keycloak.brand.resource;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * Фабрика публичного эндпоинта для брендов.
 * Регистрирует путь /realms/{realm}/brands
 */
public class PublicBrandRealmResourceProviderFactory implements RealmResourceProviderFactory {

    private static final String PROVIDER_ID = "public";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new PublicBrandRealmResourceProvider(session);
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}
