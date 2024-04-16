package ru.alamics.sso.keycloak.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;
import ru.alamics.sso.keycloak.event.listener.factory.EventFactory;
import ru.alamics.sso.keycloak.event.listener.factory.SsoEvent;
import ru.alamics.sso.keycloak.event.listener.factory.impl.EventFactoryImpl;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.service.AuthorisedUsersService;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
        String userId = event.getUserId();
        String realmId = event.getRealmId();
        String clientId = event.getClientId();

        if (userId == null || realmId == null || clientId == null) {
            log.error("userId == " + userId + ", " + "realmId == " + realmId + ", " + "clientId == " + clientId);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");


        RealmProvider model = session.realms();
        RealmModel realm = model.getRealm(event.getRealmId());
        UserModel userModel = session.users().getUserById(userId, realm);
        //  typeId - hardcode для авторизации через МП по отпечатку/коду
        if (EventType.REFRESH_TOKEN.equals(event.getType()) && clientId.equals("app_b2b")) {
            String lastAuthorizationTimeStr = userModel.getFirstAttribute("authorization_time_appb2b");
            if (lastAuthorizationTimeStr != null) {
                LocalDateTime lastAuthorizationTime = LocalDateTime.parse(lastAuthorizationTimeStr, formatter);
                if (ChronoUnit.HOURS.between(lastAuthorizationTime, now) <= 24) {
                    return;
                }
            }
            authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 7);
            userModel.setSingleAttribute("authorization_time_appb2b", now.format(formatter));

        } else if (EventType.LOGIN.equals(event.getType()) && clientId.equals("app_b2b")) {
            authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 3);
            LocalDateTime nowMinus26 = now.minusHours(26);
            userModel.setSingleAttribute("authorization_time_appb2b", nowMinus26.format(formatter));
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
