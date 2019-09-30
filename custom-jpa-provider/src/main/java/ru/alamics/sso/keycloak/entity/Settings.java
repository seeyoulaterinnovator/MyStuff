package ru.alamics.sso.keycloak.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Data
@Table(name = "SETTINGS")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Settings implements Serializable {

    private static final long serialVersionUID = -8261807382730731630L;
    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "ext_id")
    private String extId;

    @Column(name = "value")
    private String value;

    @Column(name = "desc")
    private String desc;

    @Column(name = "realm_id")
    private String realmId;
}
