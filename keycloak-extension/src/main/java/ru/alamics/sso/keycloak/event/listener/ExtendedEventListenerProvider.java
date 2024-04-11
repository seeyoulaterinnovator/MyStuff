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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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

        long now = System.currentTimeMillis();
        log.info("now is " + now);


        //  typeId - hardcode для авторизации через МП по отпечатку/коду


        RealmProvider model = session.realms();
        RealmModel realm = model.getRealm(event.getRealmId());
        UserModel userModel = session.users().getUserById(userId, realm);
        if (EventType.REFRESH_TOKEN.equals(event.getType()) && clientId.equals("app_b2b")) {
            if ((now - Long.parseLong(userModel.getAttribute("authorization_time").get(0)) < TimeUnit.DAYS.toMillis(1))) {
                return;
            }
//            if ((now - Long.parseLong(userModel.getAttribute("authorization_time").get(0)) > TimeUnit.DAYS.toMillis(1)) &&
//                    (userModel.getAttribute("number_of_ref_tokens").contains("0"))) {
                authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 7);
                userModel.setSingleAttribute("authorization_time", String.valueOf(now));
//                userModel.setSingleAttribute("number_of_ref_tokens", "1");
//            }
        } else if (EventType.LOGIN.equals(event.getType()) && clientId.equals("app_b2b")) {
            authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 3);

            long hoursToSubtract = 25;
            long millisecondsToSubtract = hoursToSubtract * 3600000; // Перевод часов в миллисекунды

            long nowMinus25 = now - millisecondsToSubtract;

            log.info("nowMinus25 is " + nowMinus25);

            userModel.setSingleAttribute("authorization_time", String.valueOf(nowMinus25));
//            userModel.setSingleAttribute("number_of_ref_tokens", "0");
        }
//        else if ((EventType.REFRESH_TOKEN.equals(event.getType()) && clientId.equals("app_b2b")) && (now - Long.parseLong(userModel.getAttribute("authorization_time").get(0)) > TimeUnit.DAYS.toMillis(1))) {
//            userModel.setSingleAttribute("number_of_ref_tokens", "0");
//        }

//        try {
//            long userAtt = Long.parseLong(userModel.getAttribute("authorization_time").get(0));
//        }
//        catch (IndexOutOfBoundsException e) {
//            log.info("userModel.getAttribute(authorization_time).get(0) = " + userModel.getAttribute("authorization_time").get(0));
//            userModel.setSingleAttribute("authorization_time", String.valueOf(now));
//
//        }
//        if (userModel.getAttribute("authorization_time").get(0) == null) {
//            log.info("userModel.setSingleAttribute");
//            userModel.setSingleAttribute("authorization_time", String.valueOf(now));
//        }

//        long userAtt = Long.parseLong(userModel.getAttribute("authorization_time").get(0));
//        log.info("userAtt = " + userAtt);

//        log.info(" now - lastRecordTime > TimeUnit.DAYS.toMillis(1) is : " + (now - lastRecordTime > TimeUnit.DAYS.toMillis(1)));

//        if (lastRecordTime == null || !isSameDay(lastRecordTime, now)) {
//            log.info("onEvent is called, lastRecordTime != null ");
////        if (lastRecordTime == null || now - lastRecordTime > TimeUnit.DAYS.toMillis(1)) {
//            if (EventType.REFRESH_TOKEN.equals(event.getType())) {
//                log.info("loging REFRESH TOKEN");
//                authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 7);
//            } else if (EventType.LOGIN.equals(event.getType()) && clientId.equals("app_b2b")) {
//                log.info("loging LOGIN APP_b2b");
//                authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 3);
//            }
//            log.info("after lastRecordTimestamps.toString() is " + lastRecordTimestamps.toString());
//            lastRecordTimestamps.put(key, now);
//            log.info("before lastRecordTimestamps.toString() is " + lastRecordTimestamps.toString());
//        }

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
