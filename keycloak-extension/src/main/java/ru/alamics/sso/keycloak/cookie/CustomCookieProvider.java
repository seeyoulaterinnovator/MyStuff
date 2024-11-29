package ru.alamics.sso.keycloak.cookie;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.impl.jose.JWT;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.cookie.CookieProvider;
import org.keycloak.cookie.CookieType;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;

import java.sql.Timestamp;

@Slf4j
public class CustomCookieProvider implements CookieProvider {

    final KeycloakSession session;

    final CookieProvider provider;

    private ApplicationProperties properties;

    private static final String TIME_UPDATE_KEYCLOAK = "updateVerKC.datetime";

    public CustomCookieProvider(KeycloakSession session, CookieProvider provider) {
        this.session = session;
        this.provider = provider;
        properties = Lookup.lookup(ApplicationProperties.class);
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
                try {
                    JsonObject payload = JWT.parse(token).getJsonObject("payload");
                    if (payload.containsKey("iat")) {
                        long timestamp = payload.getLong("iat");
                        Timestamp tokenTime = new Timestamp(timestamp*1000);
                        if(tokenTime.before(Timestamp.valueOf(properties.getProperty(TIME_UPDATE_KEYCLOAK)))) {
                            log.info("clean old cookie");
                            return null;
                        }
                    } else {
                        log.info("clean old cookie");
                        return null;
                    }
                } catch (Exception e){
                    log.trace("Fail parse token", e);
                    return null;
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
