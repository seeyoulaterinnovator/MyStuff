package ru.alamics.sso.keycloak.sessions;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.grants.OAuth2GrantType;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionProvider;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import java.util.Map;

import static ru.alamics.sso.registration.model.UserConstants.AUTH_NOTE_DIRECT_GRANT_SESSION_CLEAR_DISABLED;

public class CustomAuthenticationSessionProvider implements AuthenticationSessionProvider {
    final KeycloakSession session;

    final AuthenticationSessionProvider provider;

    public CustomAuthenticationSessionProvider(KeycloakSession session, AuthenticationSessionProvider provider) {
        this.session = session;
        this.provider = provider;
    }

    @Override
    public RootAuthenticationSessionModel createRootAuthenticationSession(RealmModel realm) {
        return provider.createRootAuthenticationSession(realm);
    }

    @Override
    public RootAuthenticationSessionModel createRootAuthenticationSession(RealmModel realm, String id) {
        return provider.createRootAuthenticationSession(realm, id);
    }

    @Override
    public RootAuthenticationSessionModel getRootAuthenticationSession(RealmModel realm, String authenticationSessionId) {
        return provider.getRootAuthenticationSession(realm, authenticationSessionId);
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
            provider.removeRootAuthenticationSession(realm, authenticationSession);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeAllExpired() {
        provider.removeAllExpired();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeExpired(RealmModel realm) {
        provider.removeExpired(realm);
    }

    @Override
    public void onRealmRemoved(RealmModel realm) {
        provider.onRealmRemoved(realm);
    }

    @Override
    public void onClientRemoved(RealmModel realm, ClientModel client) {
        provider.onClientRemoved(realm, client);
    }

    @Override
    public void updateNonlocalSessionAuthNotes(
            AuthenticationSessionCompoundId compoundId,
            Map<String, String> authNotesFragment
    ) {
        provider.updateNonlocalSessionAuthNotes(compoundId, authNotesFragment);
    }

    @Override
    public void close() {
        provider.close();
    }
}
