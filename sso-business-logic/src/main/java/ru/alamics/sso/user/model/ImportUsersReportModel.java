package ru.alamics.sso.user.model;

import lombok.Data;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;

import java.util.Date;

@Data
public class ImportUsersReportModel {

    private String id;
    private String name;
    private String realmId;
    private Date importDate;
    private int countImportUsers;
    private int countCreatedUsers;
    private int countClones;
    private ImportUsersReportStatus status;
    private String filetype;
}
