package ru.alamics.sso.keycloak.mobile.resource.credentials;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

public class RestResetCredentialsProvider implements BaseResourceProvider<RestResetCredentialsResource> {

    private final KeycloakSession session;

    public RestResetCredentialsProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public RestResetCredentialsResource getResource() {
        AdminPermissionEvaluator auth = this.initAuthByWorkingRealm(session);

        auth.users().requireManage();

        return new RestResetCredentialsResource(session);
    }

}
