package ru.alamics.sso.jpa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

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

    @ManyToOne(targetEntity = ImportUsersReportEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "import_id")
    private ImportUsersReportEntity importUsersReport;

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

    // not a column
    @Transient
    private String cleanPassword;
}
