package ru.alamics.sso.keycloak.user.resource.ls;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;
import ru.alamics.sso.user.PersonalAccountService;

@Slf4j
public class PersonalAccountResourceProvider implements BaseResourceProvider<PersonalAccountResource> {

    private final KeycloakSession session;

    public PersonalAccountResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public PersonalAccountResource getResource() {
        initAuthByWorkingRealm(session).users().requireView();
        return new PersonalAccountResource(Lookup.lookup(PersonalAccountService.class));
    }
}
