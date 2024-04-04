package ru.alamics.sso.jpa.entity.auth_reg;

public enum AuthOrRegType {
    LOG_PASS(3, "loginPasswordButton", ""),
    PHONE_CALL(2, "phoneCallButton", "incoming_call_phone_verificator"),
    SMS_CODE(1, "smsButton", "phone_verificator_sms"),
    FORGOT_PASS(6, "forgotPass", "");

    private final int id;

    private final String buttonName;

    private final String reqActProviderName;

    AuthOrRegType(int id, String name, String reqActProviderName) {
        this.id = id;
        this.buttonName = name;
        this.reqActProviderName = reqActProviderName;
    }

    public int getId() {
        return id;
    }

    public String getButtonName() {
        return buttonName;
    }

    public String getReqActProviderName() {
        return reqActProviderName;
    }
}
