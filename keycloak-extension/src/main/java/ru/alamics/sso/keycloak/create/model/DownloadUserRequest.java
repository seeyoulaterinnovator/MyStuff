package ru.alamics.sso.keycloak.create.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DownloadUserRequest {

    private String type;
    private UserParameter[] userParameters;
}
