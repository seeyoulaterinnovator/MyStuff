package ru.alamics.sso.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "APP_PROPERTIES")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppProperty {
    @Id
    @Column(name = "name")
    private String name;
    @Column(name = "value")
    private String value;
}
