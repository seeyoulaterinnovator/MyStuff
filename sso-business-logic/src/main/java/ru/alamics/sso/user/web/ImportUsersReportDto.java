package ru.alamics.sso.user.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ImportUsersReportDto {
    private String id;
    private String name;
    private String realmId;
    private String importDate;
    private int countImportUsers;
    private int countCreatedUsers;
    private int countClones;
    private String status;
}
