package ru.alamics.sso.keycloak.entity;

import lombok.*;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.common.NotificationType;
import ru.alamics.sso.keycloak.entity.common.NotificationStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "AUTO_LOCK_NOTIFICATION")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

}
