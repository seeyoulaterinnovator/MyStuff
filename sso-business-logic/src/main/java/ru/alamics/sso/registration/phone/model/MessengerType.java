package ru.alamics.sso.registration.phone.model;

public enum MessengerType {

    SMS("smsSender"),
    VIBER("viberSender");

    private final String type;

    MessengerType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
