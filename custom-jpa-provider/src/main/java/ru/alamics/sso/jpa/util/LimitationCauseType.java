package ru.alamics.sso.jpa.util;

public enum LimitationCauseType {
    SMS("SMS"), PHONE_CALL("PHONE_CALL");
    private final String cause;

    LimitationCauseType(String cause) {
        this.cause = cause;
    }

    public String getCause() {
        return cause;
    }
}
