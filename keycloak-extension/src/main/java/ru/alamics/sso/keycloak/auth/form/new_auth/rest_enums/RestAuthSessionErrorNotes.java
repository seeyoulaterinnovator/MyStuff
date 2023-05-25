package ru.alamics.sso.keycloak.auth.form.new_auth.rest_enums;

public enum RestAuthSessionErrorNotes {
    ERROR_CODE,
    UNABLE_TO_SEND,
    REST_POST,
    EMAIL_ERROR,
    DUPL_EMAIL,
    PHONE_ERROR,
    DUPL_PHONE,
    REG_ERROR;

    public String toLowerCase() {
        return this.name().toLowerCase();
    }
}
