package ru.alamics.sso.keycloak.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "system")
@Data
@NoArgsConstructor
public class System {
    @Id
    private Long id;
    @Column(name = "name")
    private String name;
}
