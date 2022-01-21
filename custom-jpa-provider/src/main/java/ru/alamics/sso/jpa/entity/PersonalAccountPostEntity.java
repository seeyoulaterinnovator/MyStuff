package ru.alamics.sso.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
