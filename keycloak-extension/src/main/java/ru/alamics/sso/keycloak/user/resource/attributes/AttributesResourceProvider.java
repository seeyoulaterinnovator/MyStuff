package ru.alamics.sso.keycloak.user.resource.attributes;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;
import ru.alamics.sso.user.UserService;
import ru.alamics.sso.util.Util;

import javax.ws.rs.NotAuthorizedException;

public class AttributesResourceProvider implements BaseResourceProvider<AttributesResource> {
    private KeycloakSession session;

    public AttributesResourceProvider (KeycloakSession session) {
        this.session = session;
    }

    @Override
    public AttributesResource getResource () {
        var evaluator = initAuth(session);
        var service = new UserService(this.session, evaluator);
        return new AttributesResource(service);
    }
}
