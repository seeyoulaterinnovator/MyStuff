package ru.alamics.sso.keycloak.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "CUSTOMER")
public class Customer {
    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
