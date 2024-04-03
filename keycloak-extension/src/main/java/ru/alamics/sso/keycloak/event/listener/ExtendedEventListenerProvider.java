package ru.alamics.sso.keycloak.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.auth_n_regi.AuthOrRegTypeNotFoundException;
import ru.alamics.sso.keycloak.event.listener.factory.EventFactory;
import ru.alamics.sso.keycloak.event.listener.factory.SsoEvent;
import ru.alamics.sso.keycloak.event.listener.factory.impl.EventFactoryImpl;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.service.AuthorisedUsersService;

import static ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil.getAuthOrRegType;

@Slf4j
public class ExtendedEventListenerProvider implements EventListenerProvider {

    private KeycloakSession session;
    private AuthorisedUsersService authorisedUsersService;
    private AuthenticationFlowContext context;

    public ExtendedEventListenerProvider(KeycloakSession session) {
        this.session = session;
    }


    @Override
    public void onEvent(Event event) {
        if (EventType.REFRESH_TOKEN.equals(event.getType())) {
            String refreshToken = event.getDetails().get("refresh_token");
            User user = UserModelUserMapper.mapToUser(context.getUser());
            String clientId = context.getAuthenticationSession().getClient().getClientId();
            AuthenticationSessionModel sessionModel = context.getAuthenticationSession();

            log.info("LOG refresh token from ExtendedEventListenerProvider");

            try {
                authorisedUsersService.saveSuccessfulAuth(user,context.getRealm().getId() , clientId, getAuthOrRegType(sessionModel));
                log.info("user: " + user.getName() + ", " + "realm: " + context.getRealm().getName() + ", " + "clientId: "
                        + clientId + ", " + "type ID: " + getAuthOrRegType(sessionModel));
            } catch (AuthOrRegTypeNotFoundException e) {
                throw new RuntimeException(e);
            }

        }

    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        EventFactory factory = new EventFactoryImpl();
        try {
            SsoEvent ssoEvent = factory.create(event, this.session);
            ssoEvent.execute();
        } catch (IllegalArgumentException e) {
            log.info("ignore event factory create exception e={}", e.getMessage());
        }

    }

    @Override
    public void close() {
    }


}
