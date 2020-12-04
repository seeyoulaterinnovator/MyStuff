package ru.alamics.sso.user.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class DownloadUserRequest {
    @NotNull
    private String type;
    private UserParameter[] userParameters;
    @NotNull
    private String[] userIds;
}
