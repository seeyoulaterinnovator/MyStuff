package ru.alamics.sso.settings;

public enum SettingConstants {
    ABSENCE_BLOCKING_DAYS("user.absence.blocking.days"),
    ABSENCE_NOTIFICATION_DAYS("user.absence.notifications.days"),
    EXPIRE_SMS_VIBER_CODE("user.expire.sms-viber.code"),
    EXPIRE_INCOMING_CALL_CODE("user.expire.incoming.call.code"),
    EXPIRE_INCOMING_CALL_EMAIL_CODE("user.expire.incoming.call.email.code"),

    HOME_PAGE("homePageSystem"),
    TIMER_INTERVAL_DURATION_PROPERTY("timerIntervalDurationProperty"),
    DEFAULT_REALM_CLIENT_ID("defaultRealmClient"),

    TIME_TOKEN_VERIFY_EMAIL("life.token.loginverify.email"),
    TIME_TOKEN_RESET_PASSWORD("life.token.reset.pass"),
    TIME_TOKEN_SET_FIRST_PASS("life.token.set.first-pass"),

    BLOCK_NOTIFICATION_OF_WARNING("block.notification.warning"),
    BLOCK_NOTIFICATION_OF_BLOCKED("block.notification.blocked"),
    BLOCK_NOTIFICATION_OF_UNLOCKING("block.notification.unlocking");

    private String key;

    SettingConstants(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
