package ru.alamics.sso.keycloak.user.resource.ls;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;
import ru.alamics.sso.user.PersonalAccountService;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@Slf4j
public class PersonalAccountResourceProvider implements BaseResourceProvider<PersonalAccountResource> {

    private final KeycloakSession session;

    public PersonalAccountResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public PersonalAccountResource getResource() {

        AdminPermissionEvaluator evaluator = initAuthByWorkingRealm(session);
        evaluator.users().requireView();
        try {
            PersonalAccountService service = (PersonalAccountService) new InitialContext().lookup("java:global/domru-sso/" + PersonalAccountService.class.getSimpleName());
            return new PersonalAccountResource(service);
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
    }
}
