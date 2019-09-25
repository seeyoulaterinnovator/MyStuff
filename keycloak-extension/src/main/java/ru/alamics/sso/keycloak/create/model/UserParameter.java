package ru.alamics.sso.keycloak.create.model;

public enum UserParameter {
    USER_ID("USER ID"),
    FIRST_NAME("Имя пользователя"),
    EMAIL("E-mail"),
    PHONE("Телефон"),
    ORGANIZATION("Организация"),
    ROLE("Роли пользователя"),
    SYSTEM("Целевая система"),
    ENABLED("Активность"),
    CUSTOMER("CUSTOMER");

    private final String name;

    UserParameter(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
