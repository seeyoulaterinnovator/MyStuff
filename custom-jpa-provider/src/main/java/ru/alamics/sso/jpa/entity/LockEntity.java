package ru.alamics.sso.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Table(name = "LOCKS")
@Entity
@Getter
@Setter
public class LockEntity {
    @Id
    @Column(name = "ID", nullable = false)
    String id;

    @Column(name = "OWNER", nullable = false)
    String owner;

    @Column(name = "ACQUIRE_DATE", nullable = false)
    Instant acquireDate;

    @Version
    @Column(name = "VERSION", nullable = false)
    Long version;
}
