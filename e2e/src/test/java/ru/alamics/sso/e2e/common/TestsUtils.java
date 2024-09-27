package ru.alamics.sso.e2e.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestsUtils {
    private static final AtomicInteger EMAIL_COUNTER = new AtomicInteger(1);

    private static final AtomicInteger PHONE_COUNTER = new AtomicInteger(1);

    public static String getQueryParameter(String url, String parameter) {
        return Arrays.stream(URI.create(url).getRawQuery().split("&"))
                .filter(p -> p.split("=")[0].equals(parameter)).map(p -> p.split("=")[1])
                .map(v -> URLDecoder.decode(v, StandardCharsets.UTF_8))
                .findFirst().orElse(null);
    }

    public static String randomEmail() {
        return String.format(
                "tester%s%d@nomail.tld",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), EMAIL_COUNTER.getAndIncrement()
        );
    }

    public static String randomPhone() {
        return String.format(
                "8%s%02d",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), PHONE_COUNTER.getAndIncrement() % 100
        );
    }
}
