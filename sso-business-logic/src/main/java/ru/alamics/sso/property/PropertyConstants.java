package ru.alamics.sso.property;

public enum PropertyConstants {
    ABSENCE_BLOCKING_DAYS("user.absence.blocking.days"),
    ABSENCE_NOTIFICATION_DAYS("user.absence.notifications.days"),
    EXPIRE_SMS_VIBER_CODE("user.expire.sms-viber.code"),
    EXPIRE_INCOMING_CALL_CODE("user.expire.incoming.call.code"),
    EXPIRE_INCOMING_CALL_EMAIL_CODE("user.expire.incoming.call.email.code");

    private String key;

    PropertyConstants(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
