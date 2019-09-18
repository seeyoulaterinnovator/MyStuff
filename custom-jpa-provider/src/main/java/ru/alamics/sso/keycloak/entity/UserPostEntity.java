package ru.alamics.sso.keycloak.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.keycloak.models.jpa.entities.UserEntity;

import javax.persistence.*;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "USER_POST")
public class UserPostEntity {
    @Id
    private String id;
    @ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "user_id")
    private UserEntity user;
    @Column(name = "toms_id")
    private String tomsId;
    @Column(name = "dmp_id")
    private String dmpId;
    @ManyToOne(targetEntity = UserPostRoleEntity.class)
    @JoinColumn(name = "role_id")
    private UserPostRoleEntity role;
    @ManyToMany
    @JoinTable(
            name = "USERPOST_EXT_SYSTEM_ROLE",
            joinColumns = @JoinColumn(name = "user_post_id"),
            inverseJoinColumns = @JoinColumn(name = "ext_system_role_id")
    )
    private Set<ExternalSystemRoleEntity> systemRoles;

    @Override
    public String toString() {
        return "UserPost{" +
                "id='" + id + '\'' +
                ", user=" + user +
                ", tomsId='" + tomsId + '\'' +
                ", dmpId='" + dmpId + '\'' +
                ", role=" + role +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserPostEntity userPost = (UserPostEntity) o;

        if (id != null ? !id.equals(userPost.id) : userPost.id != null) return false;
        if (user != null ? !user.equals(userPost.user) : userPost.user != null) return false;
        if (tomsId != null ? !tomsId.equals(userPost.tomsId) : userPost.tomsId != null) return false;
        if (dmpId != null ? !dmpId.equals(userPost.dmpId) : userPost.dmpId != null) return false;
        return role != null ? role.equals(userPost.role) : userPost.role == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (tomsId != null ? tomsId.hashCode() : 0);
        result = 31 * result + (dmpId != null ? dmpId.hashCode() : 0);
        result = 31 * result + (role != null ? role.hashCode() : 0);
        return result;
    }
}
