package ru.alamics.sso.jpa.entity.common;

public enum ImportUsersReportStatus {
    DONE("DONE"),
    IN_PROGRESS("IN PROGRESS"),
    AWAITING("AWAITING"),
    UPLOADING("UPLOADING");

    private String discription;

    ImportUsersReportStatus(String discription) {
        this.discription = discription;
    }

    public String getDiscription(){
        return discription;
    }
}
