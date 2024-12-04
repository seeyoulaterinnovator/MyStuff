package ru.alamics.sso.user.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DownloadUserRequest {
    @NotNull
    private String type;
    private UserParameter[] userParameters;
    @NotNull
    private String[] userIds;
}
