package ru.alamics.sso.keycloak.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.Set;

@Entity
@Immutable
@Table(name = "EXTERNAL_SYSTEM")
@Data
@NoArgsConstructor
public class ExternalSystemEntity {
    @Id
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "label")
    private String label;
    @OneToMany(mappedBy = "externalSystem")
    private Set<ExternalSystemRoleEntity> systemRoles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExternalSystemEntity system = (ExternalSystemEntity) o;

        if (id != null ? !id.equals(system.id) : system.id != null) return false;
        return name != null ? name.equals(system.name) : system.name == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
