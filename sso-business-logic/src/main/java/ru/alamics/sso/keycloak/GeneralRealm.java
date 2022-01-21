package ru.alamics.sso.keycloak;

import java.util.Arrays;
import java.util.List;

public class GeneralRealm {
    public static final String MASTER = "master";
    public static final String MANAGER = "manager";
    public static final List<String> REALMS = Arrays.asList(MASTER, MANAGER);
}
