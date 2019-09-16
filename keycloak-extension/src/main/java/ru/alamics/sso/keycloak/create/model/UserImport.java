package ru.alamics.sso.keycloak.create.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserImport {
    private UserRequest userRequest;
    private String org;
    private String systemName;
    private String roleName;
}
