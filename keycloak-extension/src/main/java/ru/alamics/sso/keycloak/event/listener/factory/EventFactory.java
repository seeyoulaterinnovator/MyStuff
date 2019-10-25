package ru.alamics.sso.keycloak.event.listener.factory;

import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

public interface EventFactory {

    SsoEvent create(AdminEvent adminEvent, KeycloakSession session);
}
