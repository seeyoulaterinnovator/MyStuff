package ru.alamics.sso.keycloak.mobile.resource;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

public class RestProvider implements BaseResourceProvider<RestResource> {

    private final KeycloakSession session;

    public RestProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public RestResource getResource() {
        AdminPermissionEvaluator auth = this.initAuthByWorkingRealm(session);

        auth.users().requireManage();

        return new RestResource(session);
    }

}
