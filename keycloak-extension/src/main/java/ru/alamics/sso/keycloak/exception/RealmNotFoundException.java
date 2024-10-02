package ru.alamics.sso.keycloak.exception;

import jakarta.ws.rs.NotFoundException;

public class RealmNotFoundException extends NotFoundException {
    public RealmNotFoundException() {
        super("Realm not found");
    }
}
