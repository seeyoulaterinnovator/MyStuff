package ru.alamics.sso.jpa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import ru.alamics.sso.jpa.entity.common.ImportUsersDataStatus;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "IMPORT_USERS_DATA")
public class ImportUsersDataEntity {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id;

    //@ManyToOne(targetEntity = ImportUsersReportEntity.class, fetch = FetchType.LAZY)
    @Column(name = "import_id")
    private String importUsersReport;

    @Column(name = "first_name")
    private String firstName;
    @Column(name = "email")
    private String email;
    @Column(name = "phone")
    private String phone;
    @Column(name = "toms_id")
    private String tomsId;
    @Column(name = "dmp_id")
    private String dmpId;
    @Column(name = "role")
    private String role;
    @Column(name = "systems")
    private String systems;
    @Column(name = "is_created")
    private boolean isCreated;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "errors")
    private String errors;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ImportUsersDataStatus status = ImportUsersDataStatus.AWAITING;

    // not a column
    @Transient
    private String cleanPassword;
}
