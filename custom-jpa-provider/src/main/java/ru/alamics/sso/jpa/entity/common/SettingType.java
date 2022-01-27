package ru.alamics.sso.jpa.entity.common;

public enum SettingType {

    REALM("REALM"),
    FRONT("FRONT"),
    APP("APP"),
    EMAIL("EMAIL"),
    MESSAGE("MESSAGE");

    private final String type;

    SettingType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
