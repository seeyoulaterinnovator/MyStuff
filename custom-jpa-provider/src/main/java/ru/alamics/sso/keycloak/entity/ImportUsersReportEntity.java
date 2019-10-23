package ru.alamics.sso.keycloak.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "IMPORT_USERS_REPORT")
public class ImportUsersReportEntity {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id;
    @Column(name = "name")
    private String name;
    @Column(name = "realm_id")
    private String realmId;
    @Column(name = "import_date")
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date importDate;
    @Column(name = "count_import_users")
    private int countImportUsers;
    @Column(name = "count_created_users")
    private int countCreatedUsers;
    @Column(name = "count_clones")
    private int countClones;
    @Column(name = "is_done")
    private boolean isDone;
    @OneToMany(mappedBy = "importUsersReport", cascade = CascadeType.ALL)
    private List<ImportUsersDataEntity> importUserData;
}
