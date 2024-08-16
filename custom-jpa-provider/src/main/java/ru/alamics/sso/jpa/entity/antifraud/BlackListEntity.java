package ru.alamics.sso.jpa.entity.antifraud;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.keycloak.models.jpa.entities.UserEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Table(name = "BLACK_LIST")
@Entity
@Data
@NoArgsConstructor
public class BlackListEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "email")
    private String email;
    @Column(name = "created")
    private LocalDateTime createdAt;
    @Column(name = "unblocked")
    private LocalDateTime unblockedAt;
    @Column(name = "block_duration")
    private Long blockDurationSec;
    @Column(name = "block_count")
    private Integer blockCount;
    @Column(name = "phone")
    private String phone;
    @Column(name = "limitation_cause")
    private String limitationCause;
    @Column(name = "realm")
    private String realm;
    @ManyToOne(targetEntity = UserEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
