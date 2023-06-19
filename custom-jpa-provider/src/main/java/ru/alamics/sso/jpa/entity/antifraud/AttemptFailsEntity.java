package ru.alamics.sso.jpa.entity.antifraud;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ATTEMPT_FAILS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AttemptFailsEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "phone")
    private String phone;
    @Column(name = "code")
    private String code;
    @Column(name = "realm")
    private String realm;
    @Column(name = "limitation_cause")
    private String limitationCause;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "created")
    private LocalDateTime created;
}
