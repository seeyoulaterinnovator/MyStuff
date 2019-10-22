package ru.alamics.sso.keycloak.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "IMPORT_USER_DATA")
public class ImportUserDataEntity {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id;
    @ManyToOne(targetEntity = ImportUserHistoryEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "import_id")
    private ImportUserHistoryEntity importUserHistory;
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
}
