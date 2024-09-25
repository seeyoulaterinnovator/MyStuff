package ru.alamics.sso.keycloak.manager;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ManagerRequestProperties {
    public static final String DISABLE_STRICT_ADMIN_AUTH = "X-Manager-DisableStrictAdminAuth";

    public static final String ADMIN_CONTEXT_REALM = "X-Manager-ContextRealm";
}
