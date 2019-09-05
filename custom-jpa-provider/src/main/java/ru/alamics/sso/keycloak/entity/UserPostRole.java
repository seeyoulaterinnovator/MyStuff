package ru.alamics.sso.keycloak.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "user_post_role")
@Data
@NoArgsConstructor
public class UserPostRole {
    @Id
    private Long id;
    @Column(name = "name")
    private String name;
}
