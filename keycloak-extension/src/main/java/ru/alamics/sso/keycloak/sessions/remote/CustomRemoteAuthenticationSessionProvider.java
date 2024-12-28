package ru.alamics.sso.keycloak.sessions.remote;

import org.infinispan.client.hotrod.RemoteCache;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.sessions.infinispan.InfinispanAuthenticationSessionProvider;
import org.keycloak.models.sessions.infinispan.entities.RootAuthenticationSessionEntity;
import org.keycloak.models.utils.SessionExpiration;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionProvider;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static ru.alamics.sso.registration.model.UserConstants.AUTH_NOTE_DIRECT_GRANT_SESSION_CLEAR_DISABLED;

/**
 * @see InfinispanAuthenticationSessionProvider
 */
public class CustomRemoteAuthenticationSessionProvider implements AuthenticationSessionProvider {
    protected final CustomRemoteKeycloakTransaction tx;
    private final KeycloakSession session;
    private final RemoteCache<String, RootAuthenticationSessionEntity> cache;
    private final CustomRemoteKeyGenerator keyGenerator;
    private final int authSessionsLimit;

    public CustomRemoteAuthenticationSessionProvider(
            KeycloakSession session,
            CustomRemoteKeyGenerator keyGenerator,
            RemoteCache<String, RootAuthenticationSessionEntity> cache,
            int authSessionsLimit
    ) {
        this.session = session;
        this.cache = cache;
        this.keyGenerator = keyGenerator;
        this.authSessionsLimit = authSessionsLimit;
        this.tx = new CustomRemoteKeycloakTransaction();
        session.getTransactionManager().enlistAfterCompletion(tx);
    }


    @Override
    public RootAuthenticationSessionModel createRootAuthenticationSession(RealmModel realm) {
        String id = keyGenerator.generateKeyString(session, cache);
        return createRootAuthenticationSession(realm, id);
    }

    @Override
    public RootAuthenticationSessionModel createRootAuthenticationSession(RealmModel realm, String id) {
        RootAuthenticationSessionEntity entity = new RootAuthenticationSessionEntity(id);
        entity.setRealmId(realm.getId());
        entity.setTimestamp(Time.currentTime());
        int expirationSeconds = SessionExpiration.getAuthSessionLifespan(realm);
        tx.put(cache, id, entity, expirationSeconds, TimeUnit.SECONDS);
        return wrap(realm, entity);
    }

    @Override
    public RootAuthenticationSessionModel getRootAuthenticationSession(
            RealmModel realm, String authenticationSessionId
    ) {
        RootAuthenticationSessionEntity entity = tx.get(cache, authenticationSessionId);
        return wrap(realm, entity);
    }

    /**
     * Восстановлено поведение Keycloak6 для мобильного приложения, когда нет удаления сессии аутентификации
     * после неудачной попытки получить токен через direct grant flow и наличия обязательного действия обновления пароля
     * @see ru.alamics.sso.keycloak.auth.rest.RestRequiredActionsAuthenticator#authenticate
     * @see org.keycloak.protocol.oidc.grants.ResourceOwnerPasswordCredentialsGrantType#process
     */
    @Override
    public void removeRootAuthenticationSession(RealmModel realm, RootAuthenticationSessionModel authenticationSession) {
        var authSession = session.getContext().getAuthenticationSession();
        if(authSession == null ||
                !Boolean.TRUE.toString().equals(authSession.getAuthNote(AUTH_NOTE_DIRECT_GRANT_SESSION_CLEAR_DISABLED))) {
            tx.remove(cache, authenticationSession.getId());
        }
    }

    @Override
    public void removeAllExpired() {}

    @Override
    public void removeExpired(RealmModel realm) {}

    @Override
    public void onRealmRemoved(RealmModel realm) {
        // TODO
    }

    @Override
    public void onClientRemoved(RealmModel realm, ClientModel client) {
        // TODO
    }

    @Override
    public void updateNonlocalSessionAuthNotes(
            AuthenticationSessionCompoundId compoundId, Map<String, String> authNotesFragment
    ) {}

    @Override
    public void close() {}

    protected String generateTabId() {
        return Base64Url.encode(SecretGenerator.getInstance().randomBytes(8));
    }

    private CustomRootAuthenticationSessionAdapter wrap(RealmModel realm, RootAuthenticationSessionEntity entity) {
        if(entity == null) return null;

        return new CustomRootAuthenticationSessionAdapter(session, this, cache, realm, entity, authSessionsLimit);
    }
}
