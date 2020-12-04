package ru.alamics.sso.registration.model;

import lombok.Builder;
import lombok.Data;
import org.keycloak.models.jpa.entities.UserEntity;

import javax.persistence.criteria.CriteriaBuilder;

@Data
@Builder
public class QueryUsers {
    private String search;
    private String tomsId;
    private String dmpId;

}
