package ru.alamics.sso.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class E2EUtil {
    // TODO убрать по возможности отдельный режим работы для E2E
    public static boolean isE2E() {
        return Boolean.TRUE.toString().equals(System.getenv("ERTH_SSO_E2E_ENABLED"));
    }
}
