package ru.alamics.sso.keycloak.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ExtendedEventListenerProvider implements EventListenerProvider {

    private KeycloakSession session;
    private final AuthorisedUsersService authorisedUsersService;

    private static final String AUTHORIZATION_TIME = "authorization_time_";
    private static final Set<String> clientsRedirect = Stream.of("b2b", "lkb2b", "app_b2b", "dmp-kc-sit", "wifi", "oats").collect(Collectors.toSet());
    private static final Set<String> clientsRefresh1 = Stream.of("lkb2b", "app_b2b", "wifi").collect(Collectors.toSet());
    private static final Set<String> clientsRefresh2 = Stream.of("b2b", "dmp-kc-sit", "oats").collect(Collectors.toSet());

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
        String authorization_time_type = AUTHORIZATION_TIME + clientId;

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        RealmProvider model = session.realms();
        RealmModel realm = model.getRealm(event.getRealmId());
        UserModel userModel = session.users().getUserById(userId, realm);
        //  typeId - hardcode для авторизации через МП по отпечатку/коду

        switch (event.getType()) {
            case LOGIN: {
                if (clientId.equals("app_b2b")) {
                    authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 3);
                    LocalDateTime nowMinus26 = now.minusHours(26);
                    userModel.setSingleAttribute(authorization_time_type, nowMinus26.format(formatter));
                }
            }
            break;
            case REFRESH_TOKEN: {
                if (clientsRefresh1.contains(clientId)) {
//                    String lastAuthorizationTimeStr = userModel.getFirstAttribute(authorization_time_type);
//                    if (lastAuthorizationTimeStr != null) {
//                        LocalDateTime lastAuthorizationTime = LocalDateTime.parse(lastAuthorizationTimeStr, formatter);
//                        LocalDate lastAuthorizationDate = lastAuthorizationTime.toLocalDate();
//                        LocalDate currentDate = now.toLocalDate();
//                        if (lastAuthorizationDate.isEqual(currentDate)) {
//                            return; // Если авторизация была в этот же календарный день, прерываем выполнение
//                        }
//                    }
                    authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 7);
//                    userModel.setSingleAttribute(authorization_time_type, now.format(formatter));
                }
            }
            break;
//            case CODE_TO_TOKEN: {
//                if (clientsRefresh2.contains(clientId)) {
////                    String lastAuthorizationTimeStr = userModel.getFirstAttribute(authorization_time_type);
////                    if (lastAuthorizationTimeStr != null) {
////                        LocalDateTime lastAuthorizationTime = LocalDateTime.parse(lastAuthorizationTimeStr, formatter);
////                        LocalDate lastAuthorizationDate = lastAuthorizationTime.toLocalDate();
////                        LocalDate currentDate = now.toLocalDate();
////                        if (lastAuthorizationDate.isEqual(currentDate)) {
////                            return; // Если авторизация была в этот же календарный день, прерываем выполнение
////                        }
////                    }
//                    authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 7);
////                    userModel.setSingleAttribute(authorization_time_type, now.format(formatter));
//                }
//            }
//            break;
            case CLIENT_LOGIN: {
                if (clientsRedirect.contains(clientId)) {
                    authorisedUsersService.saveSuccessfulAuthFromEventListener(userId, realmId, clientId, 8);
                }
            }
            break;
            default:
                log.info("eventType = {}", event.getType());
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
