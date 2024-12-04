package ru.alamics.sso.jpa.entity.auth_reg;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "AUTH_OR_REG_TYPE")
@Getter
@Setter
public class AuthOrRegTypeEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;

    @Column(name = "auth_or_reg_type_name")
    private String authOrRegTypeName;
}
