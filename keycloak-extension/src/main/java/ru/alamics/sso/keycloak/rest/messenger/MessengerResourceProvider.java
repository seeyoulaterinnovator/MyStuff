package ru.alamics.sso.keycloak.rest.messenger;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

public class MessengerResourceProvider implements BaseResourceProvider<MessengerResource> {

    private final KeycloakSession session;

    public MessengerResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public MessengerResource getResource() {
        AdminPermissionEvaluator auth = initAuthByWorkingRealm(session);
        auth.users().requireManage();

        return new MessengerResource(session, auth);
    }
}
