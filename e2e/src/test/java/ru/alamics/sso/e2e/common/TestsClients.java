package ru.alamics.sso.e2e.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TestsClients {
    APP(TestsRealms.E2E, "app", "secret", "http://localhost");

    final TestsRealms realm;

    final String clientId;

    final String clientSecret;

    final String redirectUri;
}
