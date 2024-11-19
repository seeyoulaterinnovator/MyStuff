package ru.alamics.sso.keycloak.util;

import io.vertx.ext.auth.impl.jose.JWT;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class TokenUtil {
    public static boolean isIdToken(String token) {
        if (token != null && !token.isBlank()) {
            try {
                return "ID".equals(JWT.parse(token).getJsonObject("payload").getString("typ"));
            } catch (Exception e) {
                log.trace(e.getMessage(), e);
            }
        }
        return false;
    }
}
