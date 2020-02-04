package ru.alamics.sso.jpa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "USER_POST_ROLE")
@Data
@NoArgsConstructor
public class UserPostRoleEntity {
    @Id
    private Long id;
    @Column(name = "name")
    private String name;
}
