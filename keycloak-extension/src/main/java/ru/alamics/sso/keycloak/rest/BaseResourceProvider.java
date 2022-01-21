package ru.alamics.sso.keycloak.rest;

import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.*;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Optional;

public interface BaseResourceProvider<T> extends RealmResourceProvider {

    @Override
    T getResource();

    @Override
    default void close() {
    }

    default AdminPermissionEvaluator initAuthByWorkingRealm(KeycloakSession session) {
        KeycloakContext context = session.getContext();
        AdminAuth auth = initAdminAuth(session);

        RealmManager realmManager = new RealmManager(session);
        KeycloakUriInfo uri = context.getUri();
        MultivaluedMap<String, String> pathParameters = uri.getPathParameters();
        String realmFromRequestName = pathParameters.getFirst("realm");
        RealmModel realmFromRequest = Optional.ofNullable(realmManager.getRealmByName(realmFromRequestName))
                .orElseThrow(() -> new NotAuthorizedException("Unknown realm in path param"));

        session.getContext().setRealm(realmFromRequest);

        return AdminPermissions.evaluator(session, realmFromRequest, auth);
    }

    default AdminPermissionEvaluator initAuth(KeycloakSession session) {
        AdminAuth auth = initAdminAuth(session);

        KeycloakContext context = session.getContext();
        AppAuthManager appAuthManager = new AppAuthManager();
        String tokenString = Optional.ofNullable(appAuthManager.extractAuthorizationHeaderToken(context.getRequestHeaders())).orElseThrow(() -> new NotAuthorizedException("Bearer"));
        AccessToken token;
        try {
            JWSInput input = new JWSInput(tokenString);
            token = input.readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            throw new NotAuthorizedException("Bearer token format error");
        }

        String issuer = Optional.ofNullable(token.getIssuer()).orElseThrow(() -> new RuntimeException("empty issuer"));
        String realmName = issuer.substring(issuer.lastIndexOf('/') + 1);

        RealmManager realmManager = new RealmManager(session);
        RealmModel realmFromToken = Optional.ofNullable(realmManager.getRealmByName(realmName))
                .orElseThrow(() -> new NotAuthorizedException("Unknown realm in token"));
        return AdminPermissions.evaluator(session, realmFromToken, auth);
    }

    default AdminAuth initAdminAuth(KeycloakSession session) {
        KeycloakContext context = session.getContext();
        AppAuthManager appAuthManager = new AppAuthManager();
        String tokenString = Optional.ofNullable(appAuthManager.extractAuthorizationHeaderToken(context.getRequestHeaders())).orElseThrow(() -> new NotAuthorizedException("Bearer"));
        AccessToken token;
        try {
            JWSInput input = new JWSInput(tokenString);
            token = input.readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            throw new NotAuthorizedException("Bearer token format error");
        }

        String issuer = Optional.ofNullable(token.getIssuer()).orElseThrow(() -> new RuntimeException("empty issuer"));
        String realmName = issuer.substring(issuer.lastIndexOf('/') + 1);

        RealmManager realmManager = new RealmManager(session);
        RealmModel realmFromToken = Optional.ofNullable(realmManager.getRealmByName(realmName))
                .orElseThrow(() -> new NotAuthorizedException("Unknown realm in token"));

        session.getContext().setRealm(realmFromToken);

        AuthenticationManager.AuthResult authResult = Optional.ofNullable(appAuthManager.authenticateBearerToken(session, realmFromToken))
                .orElseThrow(() -> new NotAuthorizedException("Bearer"));

        ClientModel client = Optional.ofNullable(realmFromToken.getClientByClientId(token.getIssuedFor()))
                .orElseThrow(() -> new NotAuthorizedException("Could not find client for authorization"));

        AdminAuth auth = new AdminAuth(realmFromToken, authResult.getToken(), authResult.getUser(), client);

        if (!auth.getRealm().equals(realmManager.getKeycloakAdminstrationRealm())
                && !auth.getRealm().equals(realmFromToken)) {
            throw new ForbiddenException();
        }

        return auth;
    }
}
