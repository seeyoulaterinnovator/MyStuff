package ru.alamics.sso.jpa.entity.common;

public enum SettingType {

    REALM("REALM"),
    FRONT("FRONT"),
    APP("APP"),
    MESSAGE("MESSAGE");

    private String type;

    SettingType(String type) {
        this.type = type;
    }

    public String getType(){
        return type;
    }
}
