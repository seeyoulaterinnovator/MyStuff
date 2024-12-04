package ru.alamics.sso.jpa.entity.auth_reg;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CLIENTS_FOR_MONITORING")
@Getter
@Setter
public class ClientsForMonitoringEntity {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "monitoring")
    private boolean monitoring;

    @Column(name = "realm")
    private String realm;

    @Column(name = "name")
    private String name;
}
