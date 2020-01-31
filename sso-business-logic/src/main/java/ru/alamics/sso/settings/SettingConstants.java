package ru.alamics.sso.settings;

public enum SettingConstants {
    ABSENCE_BLOCKING_DAYS("user.absence.blocking.days"),
    ABSENCE_NOTIFICATION_DAYS("user.absence.notifications.days"),
    EXPIRE_SMS_VIBER_CODE("user.expire.sms-viber.code"),
    EXPIRE_INCOMING_CALL_CODE("user.expire.incoming.call.code"),
    EXPIRE_INCOMING_CALL_EMAIL_CODE("user.expire.incoming.call.email.code");

    private String key;

    SettingConstants(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
