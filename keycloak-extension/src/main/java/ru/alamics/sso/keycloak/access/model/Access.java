package ru.alamics.sso.keycloak.access.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@Entity
public class Access {
    @Id
    private String id;
    private String userId;
    private String tomsId;
    private String dmpId;
    private Long roleId;
}
