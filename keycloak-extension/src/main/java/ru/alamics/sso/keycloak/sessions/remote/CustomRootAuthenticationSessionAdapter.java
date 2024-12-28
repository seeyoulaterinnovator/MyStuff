package ru.alamics.sso.keycloak.sessions.remote;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.client.hotrod.RemoteCache;
import org.keycloak.common.util.Time;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.sessions.infinispan.entities.AuthenticationSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.RootAuthenticationSessionEntity;
import org.keycloak.models.utils.SessionExpiration;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CustomRootAuthenticationSessionAdapter implements RootAuthenticationSessionModel {
    private static final Comparator<Map.Entry<String, AuthenticationSessionEntity>> TIMESTAMP_COMPARATOR =
            Comparator.comparingInt(e -> e.getValue().getTimestamp());

    private final KeycloakSession session;

    private final CustomRemoteAuthenticationSessionProvider provider;

    private final RemoteCache<String, RootAuthenticationSessionEntity> cache;

    private final RealmModel realm;

    private final RootAuthenticationSessionEntity entity;

    private final int authSessionsLimit;

    public CustomRootAuthenticationSessionAdapter(
            KeycloakSession session,
            CustomRemoteAuthenticationSessionProvider provider,
            RemoteCache<String, RootAuthenticationSessionEntity> cache,
            RealmModel realm,
            RootAuthenticationSessionEntity entity,
            int authSessionsLimit
    ) {
        this.session = session;
        this.provider = provider;
        this.cache = cache;
        this.realm = realm;
        this.entity = entity;
        this.authSessionsLimit = authSessionsLimit;
    }

    void update() {
        int expirationSeconds = getTimestamp() - Time.currentTime() + SessionExpiration.getAuthSessionLifespan(realm);
        provider.tx.replace(cache, entity.getId(), entity, expirationSeconds, TimeUnit.SECONDS);
    }

    @Override
    public String getId() {
        return entity.getId();
    }

    @Override
    public RealmModel getRealm() {
        return realm;
    }

    @Override
    public int getTimestamp() {
        return entity.getTimestamp();
    }

    @Override
    public void setTimestamp(int timestamp) {
        entity.setTimestamp(timestamp);
        update();
    }

    @Override
    public Map<String, AuthenticationSessionModel> getAuthenticationSessions() {
        Map<String, AuthenticationSessionModel> result = new HashMap<>();
        for (Map.Entry<String, AuthenticationSessionEntity> entry : entity.getAuthenticationSessions().entrySet()) {
            String tabId = entry.getKey();
            result.put(tabId , new CustomRemoteAuthenticationSessionAdapter(session, this, tabId, entry.getValue()));
        }
        return result;
    }

    @Override
    public AuthenticationSessionModel getAuthenticationSession(ClientModel client, String tabId) {
        if (client == null || tabId == null) {
            return null;
        }
        AuthenticationSessionModel authSession = getAuthenticationSessions().get(tabId);
        if (authSession != null && client.equals(authSession.getClient())) {
            session.getContext().setAuthenticationSession(authSession);
            return authSession;
        } else {
            return null;
        }
    }

    @Override
    public AuthenticationSessionModel createAuthenticationSession(ClientModel client) {
        Objects.requireNonNull(client, "client");

        Map<String, AuthenticationSessionEntity> authenticationSessions = entity.getAuthenticationSessions();
        if (authenticationSessions.size() >= authSessionsLimit) {
            String tabId = authenticationSessions.entrySet().stream().min(TIMESTAMP_COMPARATOR).map(Map.Entry::getKey)
                    .orElse(null);

            if (tabId != null) {
                log.debug("Reached limit ({}) of active authentication sessions per a root authentication session. " +
                        "Removing oldest authentication session with TabId {}.", authSessionsLimit, tabId);
                authenticationSessions.remove(tabId);
            }
        }

        AuthenticationSessionEntity authSessionEntity = new AuthenticationSessionEntity();
        authSessionEntity.setClientUUID(client.getId());

        int timestamp = Time.currentTime();
        authSessionEntity.setTimestamp(timestamp);

        String tabId = provider.generateTabId();
        authenticationSessions.put(tabId, authSessionEntity);

        entity.setTimestamp(timestamp);

        update();

        CustomRemoteAuthenticationSessionAdapter authSession = new CustomRemoteAuthenticationSessionAdapter(
                session, this, tabId, authSessionEntity
        );
        session.getContext().setAuthenticationSession(authSession);
        return authSession;
    }

    @Override
    public void removeAuthenticationSessionByTabId(String tabId) {
        if (entity.getAuthenticationSessions().remove(tabId) != null) {
            if (entity.getAuthenticationSessions().isEmpty()) {
                provider.tx.remove(cache, entity.getId());
            } else {
                entity.setTimestamp(Time.currentTime());
                update();
            }
        }
    }

    @Override
    public void restartSession(RealmModel realm) {
        entity.getAuthenticationSessions().clear();
        entity.setTimestamp(Time.currentTime());
        update();
    }
}
