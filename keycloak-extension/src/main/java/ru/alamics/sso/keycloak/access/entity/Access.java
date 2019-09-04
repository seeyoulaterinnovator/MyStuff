package ru.alamics.sso.keycloak.access.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@Entity
@Table(name = "access")
public class Access {
    @Id
    private String id;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "toms_id")
    private String tomsId;
    @Column(name = "rms_id")
    private String rmsId;
    @Column(name = "role_name")
    private String roleName;
}
