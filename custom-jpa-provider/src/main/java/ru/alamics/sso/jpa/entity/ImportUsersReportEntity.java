package ru.alamics.sso.jpa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;

import jakarta.persistence.*;
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
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ImportUsersReportStatus status;
    //@OneToMany(mappedBy = "importUsersReport", cascade = CascadeType.ALL)
    @Transient
    private List<ImportUsersDataEntity> importUserData;
    @Column(name = "file_type")
    private String filetype;
}
