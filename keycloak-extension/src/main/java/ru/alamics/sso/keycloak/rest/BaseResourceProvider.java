package ru.alamics.sso.keycloak.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;
import ru.alamics.sso.util.Util;

public interface BaseResourceProvider<T> extends RealmResourceProvider {

    @Override
    T getResource ();

    @Override
    default void close () { }

    default AdminPermissionEvaluator initAuth(KeycloakSession session) {
        var context = session.getContext();
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
