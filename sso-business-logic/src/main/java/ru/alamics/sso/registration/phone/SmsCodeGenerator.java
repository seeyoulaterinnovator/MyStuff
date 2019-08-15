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

        this.leftLimit = pow(10, length - 1);
        this.rightLimit = pow(10, length) - 1L;
        log.debug("Created smsCodeGenerator with range: [{},{}]", leftLimit, rightLimit);
    }

    // при length = 16 rightLimit при расчете через Math.pow имеет длину 17
    private long pow(int a, int b) {

        long res = 1;
        for (int i = 0; i < b; i++) {
            res = res * a;
        }

        return res;
    }

    public String getCode() {
        // не дают доступ к отправке смс. приколачиваю фиксированный код и не отправляю смс
        long generatedLong = rightLimit;
        //long generatedLong = leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
        return String.valueOf(generatedLong);
    }

}
