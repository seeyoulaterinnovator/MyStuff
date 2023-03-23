package ru.alamics.sso.jpa.entity.common;

public enum BlockType {
    MANAGER_BLOCK("manager-block"), SYSTEM_BLOCK("system-block");

    private final String type;

    BlockType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
