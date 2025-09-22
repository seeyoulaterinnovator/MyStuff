package ru.alamics.sso.keycloak.brand.resource;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * Публичный провайдер для брендов.
 * Доступен без прав админа, только GET.
 */
public class PublicBrandRealmResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    public PublicBrandRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new PublicBrandResource(session);
    }

    @Override
    public void close() {
    }
}
