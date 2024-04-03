package ru.alamics.sso.keycloak.event.listener;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import ru.alamics.sso.registration.service.AuthorisedUsersService;

public class ExtendedEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final String PROVIDER_ID = "ExtendedListener";
    private final AuthorisedUsersService authorisedUsersService;

    public ExtendedEventListenerProviderFactory(AuthorisedUsersService authorisedUsersService) {
        this.authorisedUsersService = authorisedUsersService;
    }

    @Override
    public ExtendedEventListenerProvider create(KeycloakSession session) {
        return new ExtendedEventListenerProvider(session, authorisedUsersService);
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
