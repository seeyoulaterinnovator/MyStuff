package ru.alamics.sso.keycloak.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.event.listener.factory.EventFactory;
import ru.alamics.sso.keycloak.event.listener.factory.SsoEvent;
import ru.alamics.sso.keycloak.event.listener.factory.impl.EventFactoryImpl;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.service.AuthorisedUsersService;

@Slf4j
public class ExtendedEventListenerProvider implements EventListenerProvider {

    private KeycloakSession session;
    private final AuthorisedUsersService authorisedUsersService;

    public ExtendedEventListenerProvider(KeycloakSession session) {
        this.session = session;
        this.authorisedUsersService = Lookup.lookup(AuthorisedUsersService.class);
    }


    @Override
    public void onEvent(Event event) {
//        if (EventType.REFRESH_TOKEN.equals(event.getType())) {
////            typeId - hardcode иначе не смог придумать как сохранить авторизацию через МП по отпечатку/коду
//            authorisedUsersService.saveSuccessfulAuthFromEventListener(event.getUserId(), event.getRealmId(),
//                    event.getClientId(), 7);
//        } else if (EventType.LOGIN.equals(event.getType()) && event.getClientId().equals("app_b2b")) {
//            authorisedUsersService.saveSuccessfulAuthFromEventListener(event.getUserId(), event.getRealmId(),
//                    event.getClientId(), 3);
//        } else if (EventType.LOGIN.equals(event.getType()) && event.getClientId().equals("wifi")) {
//            authorisedUsersService.saveSuccessfulAuthFromEventListener(event.getUserId(), event.getRealmId(),
//                    event.getClientId(), 3);
//        }

        String userId = event.getUserId();
        String realmId = event.getRealmId();
        String clientId = event.getClientId();
        //  typeId - hardcode иначе не смог придумать как сохранить авторизацию через МП по отпечатку/коду


        if (userId == null || realmId == null || clientId == null) {
            log.error("userId == " + userId + ", " + "realmId == " + realmId + ", " + "clientId == " + clientId);
            return;
        }

        if (EventType.REFRESH_TOKEN.equals(event.getType())) {
            authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 7);
        } else if (EventType.LOGIN.equals(event.getType()) && (clientId.equals("app_b2b") || clientId.equals("wifi"))) {
            authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 3);
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
