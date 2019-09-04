package ru.alamics.sso.keycloak.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.keycloak.models.jpa.entities.UserEntity;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_post")
public class UserPost {
    @Id
    private String id;
    @ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "user_id")
    private UserEntity user;
    @Column(name = "toms_id")
    private String tomsId;
    @Column(name = "dmp_id")
    private String dmpId;
    @ManyToOne(targetEntity = Post.class)
    @JoinColumn(name = "post_id")
    private Post role;
}
