package ru.alamics.sso.keycloak.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Entity
@Immutable
@Table(name = "EXT_SYSTEM_ROLE")
@Data
@NoArgsConstructor
public class ExternalSystemRoleEntity {
    @Id
    private Long id;
    @Column(name = "name")
    private String name;
    @ManyToOne
    @JoinColumn(name = "system_id")
    private ExternalSystemEntity externalSystem;
}
