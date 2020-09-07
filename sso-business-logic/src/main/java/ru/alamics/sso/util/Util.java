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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Util {

    public static boolean isEmpty(String val) {

        return val == null || val.length() == 0;
    }

    public static String encodeUTF8(String str) {

        return encodeCharset(str, StandardCharsets.UTF_8);
    }

    public static String encodeCharset(String str, Charset charset) {

        try {
            return URLEncoder.encode(str, charset.name());
        } catch (UnsupportedEncodingException ignore) {

        }

        return null;
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

    public static String getRedirectUrl(Set<String> redirectUris) {
        return redirectUris.stream()
                .map(redirectUrl -> {
                    if (redirectUrl.endsWith("/*")) {
                        return redirectUrl.substring(0, redirectUrl.length() - 2);
                    }
                    return redirectUrl;
                }).findFirst().get();
    }

    public static String getFileExtByFilename(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    public static String getFileExtension(String contentDisposition) {
        String finalFileName = getFileName(contentDisposition);
        return getFileExtByFilename(finalFileName);
    }

    public static String getFileName(String contentDisposition) {
        String[] contentList = contentDisposition.split(";");
        for (String filename : contentList) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                return name[1].trim().replaceAll("\"", "");
            }
        }
        return "unknown";
    }

    public static String join(Iterable<String> iterable, String separator) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        for ( String str : iterable ) {
            if ( !isFirst ) {
                sb.append( separator );
            }
            else {
                isFirst = false;
            }

            sb.append(str);
        }

        return sb.toString();
    }
}
