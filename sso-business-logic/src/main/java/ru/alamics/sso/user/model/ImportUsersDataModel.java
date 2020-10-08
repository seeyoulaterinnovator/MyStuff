package ru.alamics.sso.user.model;

import lombok.Data;

@Data
public class ImportUsersDataModel {

    private String id;
    private String reportId;
    private String firstName;
    private String email;
    private String phone;
    private String tomsId;
    private String dmpId;
    private String role;
    private String systems;
    private boolean isCreated;
    private String userId;
    private String errors;

    private String cleanPassword;
}
