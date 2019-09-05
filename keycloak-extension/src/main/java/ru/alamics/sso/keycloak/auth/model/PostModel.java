package ru.alamics.sso.keycloak.auth.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostModel {
    private String roleId;
    private String roleName;
}
