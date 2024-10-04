package ru.alamics.sso.keycloak;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GeneralRealm {
    public static final List<String> MANAGER_REALMS = List.of("manager", "e2e-manager");
}
