package ru.alamics.sso.e2e.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TestsUsers {
    TESTER(TestsRealms.E2E, "tester@nomail.tld", "qwerty", 10002408221L),
    PASSWORD_TESTER(TestsRealms.E2E, "passwordtester@nomail.tld", "qwerty", 10002408222L),
    MOBILE_TESTER(TestsRealms.E2E, "mobiletester@nomail.tld", "qwerty", 10002408223L);

    final TestsRealms realm;

    final String username;

    final String password;

    final Long tomsId;
}
