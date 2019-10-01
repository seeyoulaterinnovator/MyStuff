package ru.alamics.sso.keycloak.create.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class DownloadUserRequest {
    @NotNull
    private String type;
    @NotNull
    private UserParameter[] userParameters;
    private String[] userIds;
}
