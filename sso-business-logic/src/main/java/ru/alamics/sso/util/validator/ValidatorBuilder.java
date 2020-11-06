package ru.alamics.sso.util.validator;

public class ValidatorBuilder {

    private String dmpValue;
    private String tomsValue;
    private String emailValue;
    private String phoneValue;

    public ValidatorBuilder setDmp(String value) {
        dmpValue = value;
        return this;
    }

    public ValidatorBuilder setToms(String value) {
        tomsValue = value;
        return this;
    }

    public ValidatorBuilder setEmail(String value) {
        emailValue = value;
        return this;
    }

    public ValidatorBuilder setPhone(String value) {
        phoneValue = value;
        return this;
    }

    public ValidatorBuilder() {
    }

    public ValidatorBuilder build() {

        return this;
    }

    public String getDmpValue() {
        return dmpValue;
    }

    public String getTomsValue() {
        return tomsValue;
    }

    public String getEmailValue() {
        return emailValue;
    }

    public String getPhoneValue() {
        return phoneValue;
    }
}
