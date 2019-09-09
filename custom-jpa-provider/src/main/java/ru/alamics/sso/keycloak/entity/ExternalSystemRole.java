package ru.alamics.sso.keycloak.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "EXT_SYSTEM_ROLE")
@Data
@NoArgsConstructor
public class ExternalSystemRole {
    @Id
    private Long id;
    @Column(name = "name")
    private String name;
    @ManyToOne
    @JoinColumn(name = "system_id")
    private ExternalSystem externalSystem;
    @ManyToMany
    private Set<UserPost> userPosts;
}
