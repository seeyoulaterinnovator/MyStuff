package ru.alamics.sso.jpa.entity.common;

public enum ImportUsersDataStatus {

    DONE("DONE"),
    AWAITING("AWAITING");

    private String desc;

    ImportUsersDataStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
