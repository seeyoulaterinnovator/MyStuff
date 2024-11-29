package ru.alamics.sso.keycloak.cookie;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.impl.jose.JWT;
import jakarta.inject.Inject;
import org.keycloak.cookie.CookieProvider;
import org.keycloak.cookie.CookieType;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;

import java.sql.Timestamp;

public class CustomCookieProvider implements CookieProvider {

    final KeycloakSession session;

    final CookieProvider provider;

    @Inject
    ApplicationProperties properties;

    private static final String TIME_UPDATE_KEYCLOAK = "updateVerKC.datetime";

    public CustomCookieProvider(KeycloakSession session, CookieProvider provider) {
        this.session = session;
        this.provider = provider;
    }

    @Override
    public void set(CookieType cookieType, String value) {
        provider.set(cookieType, value);
    }

    @Override
    public void set(CookieType cookieType, String value, int maxAge) {
        provider.set(cookieType, value, maxAge);
    }

    @Override
    public String get(CookieType cookieType) {
        if(cookieType.equals(CookieType.IDENTITY)){
            if(provider.get(CookieType.IDENTITY) != null){
                String token = provider.get(CookieType.IDENTITY);
                JsonObject payload = JWT.parse(token).getJsonObject("payload");
                if(payload.getString("iat") != null) {
                    Timestamp tokenTime = Timestamp.valueOf(payload.getString("iat"));
                    if(tokenTime.before(Timestamp.valueOf(properties.getProperty(TIME_UPDATE_KEYCLOAK)))) {
                        return "";
                    }
                } else {
                    return "";
                }
            }
        }
        return provider.get(cookieType);
    }

    @Override
    public void expire(CookieType cookieType) {
        provider.expire(cookieType);
    }

    @Override
    public void close() {
        provider.close();
    }
}
