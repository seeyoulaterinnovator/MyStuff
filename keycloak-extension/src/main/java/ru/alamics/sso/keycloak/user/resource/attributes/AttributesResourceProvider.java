package ru.alamics.sso.keycloak.user.resource.attributes;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.user.UserAttributeService;

@Slf4j
public class AttributesResourceProvider implements BaseResourceProvider<AttributesResource> {
    private final KeycloakSession session;

    public AttributesResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public AttributesResource getResource() {
        initAuthByWorkingRealm(session).users().requireManage();

        return new AttributesResource(new UserAttributeService(this.session, Lookup.lookup(UserFindService.class)));


    }
}
