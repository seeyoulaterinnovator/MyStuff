package ru.alamics.sso.property;

public enum PropertyConstants {
    ABSENCE_BLOCKING_DAYS("user.absence.blocking.days"),
    ABSENCE_NOTIFICATION_DAYS("user.absence.notifications.days");

    private String key;

    PropertyConstants(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
