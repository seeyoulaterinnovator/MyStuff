package ru.alamics.sso.keycloak.create.model;

public enum UserParameter {
    EMAIL("E-mail"),
    PHONE("Телефон"),
    CUSTOMER("ID customer"),
    ROLE("Роли пользователя"),
    SYSTEM("Целевая система");

    private final String name;

    UserParameter(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
