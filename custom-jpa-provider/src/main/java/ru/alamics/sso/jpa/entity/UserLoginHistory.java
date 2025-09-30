package ru.alamics.sso.jpa.entity;

import lombok.*;
import org.keycloak.models.jpa.entities.UserEntity;


import jakarta.persistence.*;
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

    @Column(name = "is_success")
    private Boolean isSuccess;

    @Column(name = "realm")
    private String realm;

    @Column(name = "BRAND_ID")
    private String brandId;

    @Column(name = "BRAND_NAME")
    private String brandName;
}
