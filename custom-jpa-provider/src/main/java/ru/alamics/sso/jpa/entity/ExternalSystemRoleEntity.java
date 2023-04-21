package ru.alamics.sso.jpa.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Entity
@Immutable
@Table(name = "EXT_SYSTEM_ROLE")
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class ExternalSystemRoleEntity {
    @Id
    private Long id;

    @Column(name = "name")
    private String name;
    @Column(name = "realm_id")
    private String realmId;

    @ManyToOne
    @JoinColumn(name = "system_id")
    private ExternalSystemEntity externalSystem;
}
