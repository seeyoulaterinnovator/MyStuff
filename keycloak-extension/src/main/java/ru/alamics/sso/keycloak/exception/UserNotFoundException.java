package ru.alamics.sso.keycloak.exception;

import jakarta.ws.rs.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException() {
        super("User not found");
    }
}
