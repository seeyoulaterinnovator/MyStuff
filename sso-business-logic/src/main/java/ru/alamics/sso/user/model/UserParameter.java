package ru.alamics.sso.user.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum UserParameter {
    USER_ID("USER ID"),
    FIRST_NAME("Имя пользователя"),
    EMAIL("E-mail"),
    PHONE("Телефон"),
    TOMS_ID("TOMS ID"),
    DMP_ID("DMP ID"),
    MARK_BRAND_ID("MARK BRAND ID"),
    ROLE("Роль пользователя"),
    SYSTEM("Целевая система"),
    ENABLED("Активность");

    private final String desc;

    UserParameter(String desc){
        this.desc = desc;
    }

    public String getDesc(){
        return desc;
    }

    @JsonCreator // This is the factory method and must be static
    public static UserParameter fromString(String str) {
        for (UserParameter b : UserParameter.values()) {
            if (b.name().equalsIgnoreCase(str)) {
                return b;
            }
        }
        return null;
    }
}
