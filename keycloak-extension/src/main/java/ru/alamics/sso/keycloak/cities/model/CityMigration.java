package ru.alamics.sso.keycloak.cities.model;

import lombok.Data;

@Data
public class CityMigration {

    private String city;
    private String name;
    private String domain;
    private boolean bss;
}
