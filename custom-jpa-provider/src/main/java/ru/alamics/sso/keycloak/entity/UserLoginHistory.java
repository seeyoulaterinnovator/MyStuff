package ru.alamics.sso.keycloak.entity;

import lombok.*;
import org.keycloak.models.jpa.entities.UserEntity;


import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_LOGIN_HISTORY")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NamedQuery(name = "deleteFromUserLoginHistory", query = "delete from UserLoginHistory uln where uln.user = :user")
public class UserLoginHistory implements Serializable {
    private static final long serialVersionUID = 7152913924799778036L;
    public static final String DELETE_BY_USER_SQL = "delete from USER_LOGIN_HISTORY where USER_ID =:user";
    @Id
    private String id;

    @ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "logined_at")
    private LocalDateTime loginedAt;
}
