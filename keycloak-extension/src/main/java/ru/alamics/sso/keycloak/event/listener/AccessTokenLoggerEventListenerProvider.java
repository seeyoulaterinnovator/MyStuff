package ru.alamics.sso.keycloak.event.listener;


import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

import org.keycloak.events.Event;

import org.keycloak.models.KeycloakSession;

@Slf4j
public class AccessTokenLoggerEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;

    public AccessTokenLoggerEventListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        if (EventType.REFRESH_TOKEN.equals(event.getType())) {
            String refreshToken = event.getDetails().get("refresh_token");
            log.info("LOG refresh token");
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
    }

    @Override
    public void close() {
    }
}

