package ru.alamics.sso.keycloak.user.resource.userinfo;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

@Slf4j
public class UserInfoResourceProvider implements BaseResourceProvider<UserInfoResource> {
    private final KeycloakSession session;

    public UserInfoResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public UserInfoResource getResource() {
        return new UserInfoResource(initAuthByWorkingRealm(session));
    }
}
