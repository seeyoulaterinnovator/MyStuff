package ru.alamics.sso.jpa.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PERSONAL_ACCOUNT")
public class PersonalAccountEntity {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Column(name = "uuid")
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private PersonalAccountPostEntity post;

    @Column(name = "value")
    private String value;

    public PersonalAccountEntity(String uuid, PersonalAccountPostEntity post) {
        this.uuid = uuid;
        this.post = post;
    }
}
