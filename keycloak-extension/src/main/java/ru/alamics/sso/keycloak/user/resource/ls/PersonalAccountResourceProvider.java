package ru.alamics.sso.keycloak.user.resource.ls;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.user.PersonalAccountService;
import ru.alamics.sso.user.UserAttributeService;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@Slf4j
public class PersonalAccountResourceProvider implements BaseResourceProvider<PersonalAccountResource> {

    private KeycloakSession session;

    public PersonalAccountResourceProvider (KeycloakSession session) {
        this.session = session;
    }

    @Override
    public PersonalAccountResource getResource () {

        initAuthByWorkingRealm(session);
        try {
            PersonalAccountService service = (PersonalAccountService) new InitialContext().lookup("java:global/domru-sso/" + PersonalAccountService.class.getSimpleName());
            return new PersonalAccountResource(service);

        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
    }
}
