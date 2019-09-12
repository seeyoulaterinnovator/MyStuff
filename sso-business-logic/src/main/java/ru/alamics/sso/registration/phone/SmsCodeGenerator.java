package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.util.EStand;
import ru.alamics.sso.util.StandResolver;

import javax.ejb.Stateless;

@Slf4j
public class SmsCodeGenerator {

    private static final long leftLimit = 100000L;
    private static final long rightLimit = 999999L;

    public SmsCodeGenerator() {
    }

    public static String getCode(int length) {
        if (length < 1 || length > 18)
            throw new IllegalArgumentException("Length should be in bounds of [1, 18]");

        return getCode(pow(10, length - 1), pow(10, length) - 1L);
    }

    public static String getCode() {
        return getCode(leftLimit, rightLimit);
    }

    private static String getCode(long leftLimit, long rightLimit) {
        long generatedLong = leftLimit + (long) (Math.random() * (rightLimit - leftLimit));

        // локально и на дэве фиксированный код и не отправляю смс
        if (!StandResolver.ENV.isBattle()) {
            generatedLong = rightLimit;
            log.info("Stand {}, predefined code = {}", StandResolver.ENV, generatedLong);
        }

        return String.valueOf(generatedLong);
    }

    // при length = 16 rightLimit при расчете через Math.pow имеет длину 17
    private static long pow(int a, int b) {

        long res = 1;
        for (int i = 0; i < b; i++) {
            res = res * a;
        }

        return res;
    }
}
