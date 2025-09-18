package ru.alamics.sso.keycloak.brand.resource;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

public class BrandRealmResourceProvider implements BaseResourceProvider<BrandResource> {

    private final KeycloakSession session;

    public BrandRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public BrandResource getResource() {
        AdminPermissionEvaluator auth = initAuthByWorkingRealm(this.session);
        auth.users().requireView();
        return new BrandResource(session, auth);
    }

    @Override
    public void close() {
    }
}