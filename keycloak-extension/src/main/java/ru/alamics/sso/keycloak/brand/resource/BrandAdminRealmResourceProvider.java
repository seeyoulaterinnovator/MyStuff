package ru.alamics.sso.keycloak.brand.resource;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

public class BrandAdminRealmResourceProvider implements BaseResourceProvider<BrandResource> {

    private final KeycloakSession session;

    public BrandAdminRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public BrandResource getResource() {
        AdminPermissionEvaluator auth = initAuthByWorkingRealm(this.session);
        auth.users().requireManage();
        return new BrandResource(session, auth);
    }

}