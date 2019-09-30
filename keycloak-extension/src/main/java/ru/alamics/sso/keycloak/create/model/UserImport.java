package ru.alamics.sso.keycloak.create.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserImport {
    private UserRequest userRequest;
    private String org;
    private List<String> systemNames;
    private String roleName;
}
