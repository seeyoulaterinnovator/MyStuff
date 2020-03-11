package ru.alamics.sso.util;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.NotAuthorizedException;

public class Util {

    public static boolean isEmpty(String val) {

        return val == null || val.length() == 0;
    }

    public static void validateToken(String tokenString, final KeycloakSession session) {
        if (tokenString == null) throw new NotAuthorizedException("Bearer");
        AccessToken token;
        try {
            JWSInput input = new JWSInput(tokenString);
            token = input.readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            throw new NotAuthorizedException("Bearer token format error");
        }

        String realmName = token.getIssuer().substring(token.getIssuer().lastIndexOf('/') + 1);
        RealmManager realmManager = new RealmManager(session);
        RealmModel realmFromToken = realmManager.getRealmByName(realmName);
        if (realmFromToken == null) {
            throw new NotAuthorizedException("Unknown realm in token");
        }
    }

    public static UserAdapter getUserAdapter(KeycloakSession session, UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        return new UserAdapter(session,
                session.getContext().getRealm(),
                session.getProvider(JpaConnectionProvider.class).getEntityManager(),
                userEntity);
    }

    public static String getCleanUserPhone(String phone) {
        if (Validation.isBlank(phone)) {
            return null;
        }
        return phone.replaceAll("[^0-9]+", "");
    }

}
