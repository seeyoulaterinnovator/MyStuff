package ru.alamics.sso.util;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.OAuth2Constants;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.validation.Validation;
import ru.alamics.sso.jpa.util.CollectionUtils;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static ru.alamics.sso.registration.model.UserConstants.HIDDEN_HEADER;
import static ru.alamics.sso.registration.model.UserConstants.I_FRAME;

@Slf4j
public class Util {

    public static final String REGEX_EMAIL = "^[\\w-+.]+@\\w[\\w-.]{0,66}\\.[a-z]{2,16}$";
    public static final String REGEX_PASSWORD = "^(?=.{8,16}$)(?=.*[A-Z])(?=.*\\d)[0-9a-zA-Z^&*%$@#\\-!.\\[\\]_].*$";
    public static String TRUE_STR = "1";
    public static String FALSE_STR = "0";
    public final static String CLIENT_B2B = "b2b";

    public static boolean isPasswordGrandType(KeycloakSession session) {
        HttpRequest contextObject = session.getContext().getContextObject(HttpRequest.class);
        MultivaluedMap<String, String> parameters = contextObject.getDecodedFormParameters();
        if (!CollectionUtils.isEmpty(parameters)) {
            return OAuth2Constants.PASSWORD.equals(parameters.getFirst(OIDCLoginProtocol.GRANT_TYPE_PARAM));
        } else {
            return false;
        }
    }

    public static boolean isRegistrationGrandType(KeycloakSession session) {
        HttpRequest contextObject = session.getContext().getContextObject(HttpRequest.class);
        MultivaluedMap<String, String> parameters = contextObject.getDecodedFormParameters();
        if (!CollectionUtils.isEmpty(parameters)) {
            return "registration".equals(parameters.getFirst(OIDCLoginProtocol.GRANT_TYPE_PARAM));
        } else {
            return false;
        }
    }

    public static boolean isFrameByCurrentRequest(KeycloakSession session) {
        MultivaluedMap<String, String> queryParameters = session.getContext().getUri().getQueryParameters();
        return queryParameters != null && (queryParameters.get(I_FRAME) != null || queryParameters.get(HIDDEN_HEADER) != null);
    }

    public static String getFormatNumber(String rawPhone) {
        return String.format("+%s %s %s %s %s", rawPhone.charAt(0), rawPhone.substring(1, 4), rawPhone.substring(4, 7), rawPhone.substring(7, 9),
                rawPhone.substring(9, 11));
    }

    public static boolean isFrameByReferer(KeycloakSession session) {
        //Признак того, что вызов формы ведется в iframe
        String referer = session.getContext().getRequestHeaders().getHeaderString("referer");

        if (referer == null) {
            return false;
        }

        try {
            referer = URLDecoder.decode(referer, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            log.warn("Referer is not decoded={}", referer);
        }

        return referer.contains(I_FRAME + "=1") || referer.contains(HIDDEN_HEADER + "=true");
    }

    public static boolean isFrame(KeycloakSession session) {
        return isFrameByCurrentRequest(session) || isFrameByReferer(session);
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

    public static boolean isEmpty(String val) {
        return val == null || val.length() == 0;
    }

    public static String join(Iterable<String> iterable, String separator) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        for (String str : iterable) {
            if (!isFirst) {
                sb.append(separator);
            } else {
                isFirst = false;
            }

            sb.append(str);
        }

        return sb.toString();
    }

    public static String getRealm(String searchRealm, String rawPath) {
        if (isEmpty(searchRealm)) {
            int beginIndex = rawPath.indexOf("/realms/") + "/realms/".length();
            String realm = rawPath.substring(beginIndex, rawPath.indexOf("/", beginIndex));
            searchRealm = isEmpty(realm) ? "user" : realm;
        }
        return searchRealm;
    }

    public static void setTimeout(Runnable task, int timeoutInSeconds)
    {
        final Timer timer = new Timer("Timeout timer");

        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                task.run();
            }
        }, 1000 * timeoutInSeconds);
    }

}
