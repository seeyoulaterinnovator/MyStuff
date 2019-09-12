package ru.alamics.sso.keycloak.response;

public enum ResponseStatus {
    SUCCESS("success"),
    FAIL("fail"),
    ERROR("exception");

    private String value;

    ResponseStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
