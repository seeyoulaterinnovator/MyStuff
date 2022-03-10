package ru.alamics.sso.keycloak.mobile.resource;

import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

public class MobileApplicationProvider implements BaseResourceProvider<MobileApplicationResource> {

    private final KeycloakSession session;

    public MobileApplicationProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public MobileApplicationResource getResource() {
        AdminPermissionEvaluator auth = this.initAuthByWorkingRealm(session);

        auth.users().requireManage();

        KeycloakContext context = session.getContext();

        AdminEventBuilder adminEventBuilder = new AdminEventBuilder(context.getRealm(), auth.adminAuth(), session, context.getConnection());

        return new MobileApplicationResource(session, adminEventBuilder);
    }

}
