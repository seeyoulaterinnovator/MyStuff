package ru.alamics.sso.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TraceUtil {
    public static boolean isTraceEnabled() {
        return !StandResolver.isBattle() && Boolean.TRUE.toString().equals(System.getenv("ERTH_TRACE_ENABLED"));
    }
}
