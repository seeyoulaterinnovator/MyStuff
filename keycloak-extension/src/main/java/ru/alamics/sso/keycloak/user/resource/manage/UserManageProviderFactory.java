package ru.alamics.sso.keycloak.user.resource.manage;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class UserManageProviderFactory implements BaseResourceProviderFactory {
    private static final String ID = "manage";


    @Override
    public RealmResourceProvider create (KeycloakSession session) {
        return new UserManageResourceProvider(session);
    }

    @Override
    public String getId () {
        return ID;
    }
}
