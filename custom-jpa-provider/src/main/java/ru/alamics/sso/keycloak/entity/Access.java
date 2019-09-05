package ru.alamics.sso.keycloak.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.Set;

@Entity
@Immutable
@Table(name = "ACCESS_NAME")
@Data
@NoArgsConstructor
public class Access {
    @Id
    private Long id;
    @Column(name = "name")
    private String name;
    @ManyToMany(mappedBy = "access")
    private Set<UserPost> userPosts;
}
