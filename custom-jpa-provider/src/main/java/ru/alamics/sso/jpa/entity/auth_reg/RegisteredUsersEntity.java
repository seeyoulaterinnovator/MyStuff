package ru.alamics.sso.jpa.entity.auth_reg;

import lombok.Getter;
import lombok.Setter;
import org.keycloak.models.jpa.entities.UserEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "REGISTERED_USERS")
@Getter
@Setter
public class RegisteredUsersEntity {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(targetEntity = UserEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "realm")
    private String realm;

    @ManyToOne(targetEntity = AuthOrRegTypeEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "reg_type")
    private AuthOrRegTypeEntity regType;

    @Column(name = "registered")
    private LocalDateTime registered;

    @ManyToOne(targetEntity = ClientsForMonitoringEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "client")
    private ClientsForMonitoringEntity client;

}
