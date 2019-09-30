package ru.alamics.sso.keycloak.auth.link.token;

import org.keycloak.authentication.actiontoken.DefaultActionToken;

public class AuthLinkActionToken extends DefaultActionToken {

    public static final String TOKEN_TYPE = "auth-link-token";

    public AuthLinkActionToken(String userId, int absoluteExpirationInSecs, String compoundAuthenticationSessionId) {
        super(userId, TOKEN_TYPE, absoluteExpirationInSecs, null, compoundAuthenticationSessionId);
    }

    private AuthLinkActionToken() {
        // Required to deserialize from JWT
        super();
    }
}
