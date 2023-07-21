package ru.alamics.sso.jpa.entity.auth_reg;

import lombok.Getter;
import lombok.Setter;
import org.keycloak.models.jpa.entities.UserEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AUTHORISED_USERS")
@Getter
@Setter
public class AuthorisedUsersEntity {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(targetEntity = UserEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "realm")
    private String realm;

    @ManyToOne(targetEntity = AuthOrRegTypeEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "auth_type")
    private AuthOrRegTypeEntity authType;

    @Column(name = "authorised")
    private LocalDateTime authorised;

    @ManyToOne(targetEntity = ClientsForMonitoringEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "client")
    private ClientsForMonitoringEntity client;

}
