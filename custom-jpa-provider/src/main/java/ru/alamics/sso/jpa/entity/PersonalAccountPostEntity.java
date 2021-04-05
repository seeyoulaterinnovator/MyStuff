package ru.alamics.sso.jpa.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PERSONAL_ACCOUNT_POST")

public class PersonalAccountPostEntity {

    @Id
    @Column(name = "post_id")
    private String postId;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private Set<PersonalAccountEntity> accounts;
}
