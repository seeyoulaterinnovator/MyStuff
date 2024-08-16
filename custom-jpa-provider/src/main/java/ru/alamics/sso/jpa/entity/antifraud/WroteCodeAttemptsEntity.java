package ru.alamics.sso.jpa.entity.antifraud;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.keycloak.models.jpa.entities.UserEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "WROTE_CODE_ATTEMPTS")
@Getter
@Setter
public class WroteCodeAttemptsEntity {

    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "phone")
    private String phone;
    @Column(name = "realm")
    private String realm;
    @Column(name = "code")
    private String code;
    @Column(name = "user_code")
    private String userCode;
    @Column(name = "type_send")
    private String typeSend;
    @Column(name = "created")
    private LocalDateTime created;
    @ManyToOne(targetEntity = UserEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private UserEntity user;

}
