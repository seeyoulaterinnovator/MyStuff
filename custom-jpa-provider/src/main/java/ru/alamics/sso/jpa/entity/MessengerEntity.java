package ru.alamics.sso.jpa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
