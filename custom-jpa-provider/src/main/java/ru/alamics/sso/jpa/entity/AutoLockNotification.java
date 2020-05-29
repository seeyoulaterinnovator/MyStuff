package ru.alamics.sso.jpa.entity;

import lombok.*;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.jpa.entity.common.NotificationType;
import ru.alamics.sso.jpa.entity.common.NotificationStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "AUTO_LOCK_NOTIFICATION")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@NamedQuery(name = "deleteFromAutoLockNotif", query = "delete from AutoLockNotification aln where aln.user = :user")
public class AutoLockNotification implements Serializable {
    public static final String DELETE_BY_USER_SQL = "delete from AUTO_LOCK_NOTIFICATION where USER_ID =:user";

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

    @Version
    private Long version;
}
