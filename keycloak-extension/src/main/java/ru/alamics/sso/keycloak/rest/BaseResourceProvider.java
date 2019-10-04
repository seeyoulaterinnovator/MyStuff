package ru.alamics.sso.keycloak.rest;

import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import java.util.Optional;

public interface BaseResourceProvider<T> extends RealmResourceProvider {

    @Override
    T getResource ();

    @Override
    default void close () { }

    default AdminAuth initAuth(KeycloakSession session) {
        var context = session.getContext();
        var appAuthManager = new AppAuthManager();
        String tokenString = Optional.ofNullable(appAuthManager.extractAuthorizationHeaderToken(context.getRequestHeaders())).orElseThrow(() -> new NotAuthorizedException("Bearer"));

        AccessToken token;
        try {
            JWSInput input = new JWSInput(tokenString);
            token = input.readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            throw new NotAuthorizedException("Bearer token format error");
        }

        var issuer = Optional.ofNullable(token.getIssuer()).orElseThrow(() -> new RuntimeException("empty issuer"));
        String realmName = issuer.substring(issuer.lastIndexOf('/') + 1);

        var realmManager = new RealmManager(session);
        var realmFromToken = Optional.ofNullable(realmManager.getRealmByName(realmName))
                .orElseThrow(() -> new NotAuthorizedException("Unknown realm in token"));

        session.getContext().setRealm(realmFromToken);//FIXME Подставляем реалм из его токена и валидируем относительно его реалма, иначе authResult кинет NPE, мб возможно сделать аккауратней

        var authResult = Optional.ofNullable(appAuthManager.authenticateBearerToken(session, realmFromToken))
                .orElseThrow(() -> new NotAuthorizedException("Bearer"));

        var client = Optional.ofNullable(realmFromToken.getClientByClientId(token.getIssuedFor()))
                .orElseThrow(() -> new NotAuthorizedException("Could not find client for authorization"));

        var auth = new AdminAuth(realmFromToken, authResult.getToken(), authResult.getUser(), client);

        var uri = context.getUri();
        var pathParameters = uri.getPathParameters();
        var realmFromRequestName = pathParameters.getFirst("realm");
        var realmFromRequest = Optional.ofNullable(realmManager.getRealmByName(realmFromRequestName))
                .orElseThrow(() -> new NotAuthorizedException("Unknown realm in token"));

        AdminPermissions.evaluator(session, realmFromRequest, auth).users().requireManage();//Проверяем права пользователя на редактирование реалма в которй он сделал запрос

        if (!auth.getRealm().equals(realmManager.getKeycloakAdminstrationRealm())
                && !auth.getRealm().equals(realmFromToken)) {
            throw new ForbiddenException();
        }

        return auth;
    }
}
