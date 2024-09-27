package ru.alamics.sso.e2e.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TestsRealms {
    E2E("e2e");

    final String id;
}
