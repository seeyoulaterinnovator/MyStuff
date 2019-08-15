package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;

import javax.ejb.Stateless;

@Slf4j
@Stateless
public class SmsCodeGenerator {

    private final long leftLimit;
    private final long rightLimit;

    public SmsCodeGenerator() {
        this.leftLimit = 100000;
        this.rightLimit = 999999;
        log.debug("Created smsCodeGenerator with default range: [{},{}]", leftLimit, rightLimit);
    }

    public SmsCodeGenerator(int length) {
        if (length < 1 || length > 18)
            throw new IllegalArgumentException("Length should be in bounds of [1, 18]");

        this.leftLimit = (long) Math.pow(10d, length - 1d);
        this.rightLimit = (long) (Math.pow(10d, length) - 1L);
        log.debug("Created smsCodeGenerator with range: [{},{}]", leftLimit, rightLimit);
    }

    public String getCode() {
        // не дают доступ к отправке смс. приколачиваю фиксированный код и не отправляю смс
        long generatedLong = rightLimit;
        //long generatedLong = leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
        return String.valueOf(generatedLong);
    }

}
