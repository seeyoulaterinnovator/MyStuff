package ru.alamics.sso.jpa.entity.antifraud;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "ATTEMPT_FAILS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttemptFailsEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "phone")
    private String phone;
    @Column(name = "code")
    private String code;
    @Column(name = "realm")
    private String realm;
    @Column(name = "limitation_cause")
    private String limitationCause;
    @Column(name = "created")
    private LocalDateTime created;
}
