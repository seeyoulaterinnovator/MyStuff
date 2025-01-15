package ru.alamics.sso.keycloak.cache;

import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.keycloak.Config;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

@RequiredArgsConstructor
public class CustomCacheResourceProvider implements BaseResourceProvider<CustomCacheResource> {
    final KeycloakSession session;

    @Override
    public CustomCacheResource getResource() {
        AdminPermissionEvaluator auth = initAuthByWorkingRealm(session);
        auth.realm().requireManageRealm();
        RealmModel realm = session.getContext().getRealm();
        if(!Config.getAdminRealm().equals(realm.getName())) throw new ForbiddenException();
        return new CustomCacheResource(
                session,
                new AdminEventBuilder(realm, auth.adminAuth(), session, session.getContext().getConnection())
                        .realm(realm)
                        .resource(ResourceType.REALM)
        );
    }
}
