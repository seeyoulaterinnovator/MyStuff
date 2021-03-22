package ru.alamics.sso.keycloak.event.listener.factory.impl;

import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.event.listener.factory.EventFactory;
import ru.alamics.sso.keycloak.event.listener.factory.SsoEvent;

public class EventFactoryImpl implements EventFactory {

    @Override
    public SsoEvent create(AdminEvent adminEvent, KeycloakSession session) {
        SsoEvent event = null;

        if (adminEvent.getOperationType() == OperationType.CREATE
                && adminEvent.getResourceType().equals(ResourceType.USER)) {
            event = new SsoUserCreateEvent(adminEvent, session);
        }

        if (adminEvent.getOperationType() == OperationType.UPDATE
                && adminEvent.getResourceType().equals(ResourceType.USER)) {
            event = new SsoUserUpdateEvent(adminEvent, session);
        }

        if (adminEvent.getOperationType() == OperationType.ACTION
                && adminEvent.getResourceType().equals(ResourceType.USER)) {
            event = new SsoUserCustomEvent(adminEvent, session);
        }

        if (event == null) {
            throw new IllegalArgumentException("Illegal argument type");
        }
        return event;
    }
}
