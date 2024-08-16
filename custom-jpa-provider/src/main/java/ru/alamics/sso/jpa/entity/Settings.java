package ru.alamics.sso.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.alamics.sso.jpa.entity.common.SettingType;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

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

    @Column(name = "unit")
    @Enumerated(EnumType.STRING)
    private TimeUnit unit;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private SettingType type;
}
