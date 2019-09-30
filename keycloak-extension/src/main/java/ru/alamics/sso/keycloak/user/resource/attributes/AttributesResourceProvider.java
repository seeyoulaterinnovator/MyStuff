package ru.alamics.sso.keycloak.user.resource.attributes;

import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;
import ru.alamics.sso.user.UserAttributeService;

public class AttributesResourceProvider implements BaseResourceProvider<AttributesResource> {
    private KeycloakSession session;

    public AttributesResourceProvider (KeycloakSession session) {
        this.session = session;
    }

    @Override
    public AttributesResource getResource () {
        var evaluator = initAuth(session);
        var service = new UserAttributeService(this.session, evaluator);
        return new AttributesResource(service);
    }
}
