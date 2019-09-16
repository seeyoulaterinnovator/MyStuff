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
public class ExternalSystem {
    @Id
    private Long id;
    @Column(name = "name")
    private String name;
    @OneToMany(mappedBy = "externalSystem")
    private Set<ExternalSystemRole> systemRoles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExternalSystem system = (ExternalSystem) o;

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
