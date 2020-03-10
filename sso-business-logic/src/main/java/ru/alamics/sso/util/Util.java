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
import ru.alamics.sso.registration.AttributeFormatException;

import javax.validation.ValidationException;
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

    public static void validateUserPhone(String phone) throws AttributeFormatException {
        if (phone == null || !phone.matches("[\\d]+") || !phone.startsWith("7") || phone.length() != 11) {
            throw new AttributeFormatException("phone");
        }
    }

    public static void validateUserPhoneAndEmail(String email, String phone) {
        try {
            validateUserPhone(phone);
        } catch (AttributeFormatException e) {
            throw new ValidationException("Phone is not valid");
        }

        if (email == null || !email.contains("@") || !email.substring(0, 1).matches("([\\w[\\s]])+")
                || email.substring(0, 1).matches("[\\d]+") || email.contains(" ") ||
                !email.substring(email.indexOf("@") + 1, email.indexOf("@") + 2).matches("([\\w[\\s]])+")) {
            throw new ValidationException("Email is not valid");
        }
    }

    public static void validateId(String id) {
        if (id == null || id.isBlank() || !id.matches("[0-9]+")) {
            throw new ValidationException(String.format("ID is not valid : id=\"%s\"", id));
        }
    }
}
