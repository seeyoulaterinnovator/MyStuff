package ru.alamics.sso.e2e.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TestsRealms {
    MASTER("master"),
    E2E("e2e"),
    E2E_MANAGER("e2e-manager");

    final String id;
}
