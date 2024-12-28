package ru.alamics.sso.keycloak.sessions.remote;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.client.hotrod.RemoteCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.KeycloakModelUtils;

/**
 * @see org.keycloak.models.sessions.infinispan.util.InfinispanKeyGenerator
 */
@Slf4j
public class CustomRemoteKeyGenerator {
    public String generateKeyString(KeycloakSession session, RemoteCache<String, ?> cache) {
        return KeycloakModelUtils.generateId();
    }
}
