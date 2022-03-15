package ru.alamics.sso.keycloak.mobile.resource.credentials;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

public class RestCredentialsProvider implements BaseResourceProvider<RestCredentialsResource> {

    private final KeycloakSession session;

    public RestCredentialsProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public RestCredentialsResource getResource() {
        AdminPermissionEvaluator auth = this.initAuthByWorkingRealm(session);

        auth.users().requireManage();

        return new RestCredentialsResource(session);
    }

}
