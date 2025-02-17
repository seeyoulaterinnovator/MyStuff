package ru.alamics.sso.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

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
    private Instant updateTime;
}
