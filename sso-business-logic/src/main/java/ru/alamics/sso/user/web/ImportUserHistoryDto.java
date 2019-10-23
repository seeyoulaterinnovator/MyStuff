package ru.alamics.sso.user.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import ru.alamics.sso.keycloak.entity.ImportUserDataEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ImportUserHistoryDto {
    private String id;
    private String name;
    private String realmId;
    private String importDate;
    private int countImportUsers;
    private int countCreatedUsers;
    private int countClones;
    private boolean isDone;
}
