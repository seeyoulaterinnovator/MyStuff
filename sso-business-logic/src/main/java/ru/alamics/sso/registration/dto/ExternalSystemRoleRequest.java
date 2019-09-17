package ru.alamics.sso.registration.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class ExternalSystemRoleRequest {
    @NotNull
    private String userPostId;
    @NotNull
    private Long systemRoleId;
}
