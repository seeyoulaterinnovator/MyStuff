package ru.alamics.sso.keycloak.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.common.NotificationType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "auto_lock_notification")
@Getter
@Setter
@NoArgsConstructor
public class AutoLockNotification implements Serializable {
    private static final long serialVersionUID = -5664166927419170550L;

    @Id
    private String id;

    @ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "sended_at")
    private LocalDateTime sendedAt;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private NotificationType type;

}
