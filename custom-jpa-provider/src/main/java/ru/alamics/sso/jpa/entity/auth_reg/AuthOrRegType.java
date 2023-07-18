package ru.alamics.sso.jpa.entity.auth_reg;

public enum AuthOrRegType {
    LOG_PASS(3, "log_pass"),
    PHONE_CALL(2, "phone_call"),
    SMS_CODE(1, "sms_code");

    private final int id;

    private final String name;

    AuthOrRegType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }


}
