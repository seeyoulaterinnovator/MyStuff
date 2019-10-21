package ru.alamics.sso.user.model;

public enum UserParameter {
    USER_ID("USER ID"),
    FIRST_NAME("Имя пользователя"),
    EMAIL("E-mail"),
    PHONE("Телефон"),
    TOMS_ID("TOMS ID"),
    DMP_ID("DMP ID"),
    ROLE("Роли пользователя"),
    SYSTEM("Целевая система"),
    ENABLED("Активность");

    private final String name;

    UserParameter(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
