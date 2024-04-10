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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ExtendedEventListenerProvider implements EventListenerProvider {

    private KeycloakSession session;
    private final AuthorisedUsersService authorisedUsersService;
    private Map<String, Long> lastRecordTimestamps = new ConcurrentHashMap<>();

    public ExtendedEventListenerProvider(KeycloakSession session) {
        this.session = session;
        this.authorisedUsersService = Lookup.lookup(AuthorisedUsersService.class);
    }


    @Override
    public void onEvent(Event event) {
        String userId = event.getUserId();
        String realmId = event.getRealmId();
        String clientId = event.getClientId();

        if (userId == null || realmId == null || clientId == null) {
            log.error("userId == " + userId + ", " + "realmId == " + realmId + ", " + "clientId == " + clientId);
            return;
        }

        String key = userId + ":" + clientId;
        long now = System.currentTimeMillis();
        Long lastRecordTime = lastRecordTimestamps.get(key);

        //  typeId - hardcode иначе не смог придумать как сохранить авторизацию через МП по отпечатку/коду
//        if (EventType.REFRESH_TOKEN.equals(event.getType())) {
//            authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 7);
//        } else if (EventType.LOGIN.equals(event.getType()) && clientId.equals("app_b2b")) {
//            authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 3);
//        }

        if (lastRecordTime == null || now - lastRecordTime > TimeUnit.DAYS.toMillis(1)) {
            if (EventType.REFRESH_TOKEN.equals(event.getType())) {
                authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 7);
            } else if (EventType.LOGIN.equals(event.getType()) && clientId.equals("app_b2b")) {
                authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 3);
            }
            lastRecordTimestamps.put(key, now);
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
