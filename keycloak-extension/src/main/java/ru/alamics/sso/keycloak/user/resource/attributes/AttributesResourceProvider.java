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
        var evaluator = initAuth();
        var service = new UserService(this.session, evaluator);
        return new AttributesResource(service);
    }

    private AdminPermissionEvaluator initAuth() {
        var context = this.session.getContext();
        var requestHeaders = context.getRequestHeaders();
        String tokenString = new AppAuthManager().extractAuthorizationHeaderToken(requestHeaders);
        Util.validateToken(tokenString, session);
        var realm = context.getRealm();
        AuthenticationManager.AuthResult authResult = new AppAuthManager()
                .authenticateBearerToken(session, realm, session.getContext().getUri(), session.getContext().getConnection(), requestHeaders);
        var client = context.getClient();
        var auth = new AdminAuth(realm, authResult.getToken(), authResult.getUser(), client);
        return AdminPermissions.evaluator(session, realm, auth);
    }

}
