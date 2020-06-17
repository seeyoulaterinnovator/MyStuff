package ru.alamics.sso.keycloak.user.resource.attributes;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.user.UserAttributeService;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@Slf4j
public class AttributesResourceProvider implements BaseResourceProvider<AttributesResource> {
    private KeycloakSession session;

    public AttributesResourceProvider (KeycloakSession session) {
        this.session = session;
    }

    @Override
    public AttributesResource getResource () {
        initAuthByWorkingRealm(session);
        UserFindService userFindService = null;
        try {
            userFindService = (UserFindService) new InitialContext().lookup("java:global/domru-sso/" + UserFindService.class.getSimpleName());
            UserAttributeService service = new UserAttributeService(this.session, userFindService);
            return new AttributesResource(service);
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }

    }
}
