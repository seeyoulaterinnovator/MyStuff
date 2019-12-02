package ru.alamics.sso.keycloak.entity.common;

public enum ImportUsersReportStatus {
    DONE("DONE"),
    IN_PROGRESS("IN PROGRESS"),
    AWAITING("AWAITING");

    private String discription;

    ImportUsersReportStatus(String discription) {
        this.discription = discription;
    }

    public String getDiscription(){
        return discription;
    }
}
