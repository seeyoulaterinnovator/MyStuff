package ru.alamics.sso.keycloak.user.resource.userinfo;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class UserInfoProviderFactory implements BaseResourceProviderFactory {
    private static final String ID = "user-info";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new UserInfoResourceProvider(session);
    }

    @Override
    public String getId() {
        return ID;
    }
}
