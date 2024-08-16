package ru.alamics.sso.jpa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "MESSENGER_TYPE")
@Data
@NoArgsConstructor
public class MessengerEntity {

    @Id
    private UUID id;
    @Column(name = "name")
    private String name;
    @Column(name = "label")
    private String label;
}
